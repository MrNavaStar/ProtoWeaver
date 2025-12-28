package me.mrnavastar.protoweaver.client.netty;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.StringUtil;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;

import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This trust manager is based off the {@link io.netty.handler.ssl.util.FingerprintTrustManagerFactory} but modified to
 * function in the manner that ssh does (By always trusting the first connection and saving its fingerprint).
 */
public class ProtoTrustManager implements X509TrustManager {

    private static final FastThreadLocal<MessageDigest> localMessageDigest = new FastThreadLocal<>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("SHA256");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("Unsupported hash algorithm", e);
            }
        }
    };

    @FunctionalInterface
    public interface CertificateEventHandler {
        void handle(byte[] expected, byte[] actual);
    }

    private final File hostsFile;
    private final String hostId;
    private byte[] trusted = null;
    private final List<CertificateEventHandler> certificateRejectionHandlers = new CopyOnWriteArrayList<>();
    private static final Object lock = new Object();

    public ProtoTrustManager(String host, int port, String file) {
        hostsFile = new File(file + File.separator + "protoweaver.hosts");
        this.hostId = host + ":" + port;
        if (!hostsFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(hostsFile))) {
            reader.lines().filter(l -> l.startsWith(host + ":" + port)).findFirst()
                    .ifPresent(l -> trusted = StringUtil.decodeHexDump(l.split("=")[1].strip()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onCertificateRejected(@NonNull CertificateEventHandler handler) {
        certificateRejectionHandlers.add(handler);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String s) {
        checkTrusted(chain);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String s) {
        checkTrusted(chain);
    }

    @SneakyThrows
    private void checkTrusted(X509Certificate[] chain) {
        MessageDigest md = localMessageDigest.get();
        md.reset();

        byte[] expected;
        byte[] actual = md.digest(chain[0].getEncoded());
        synchronized (lock) {
            if (trusted == null) {
                hostsFile.getParentFile().mkdirs();
                hostsFile.createNewFile();

                @Cleanup BufferedWriter writer = new BufferedWriter(new FileWriter(hostsFile, true));
                writer.append(hostId).append("=").append(StringUtil.toHexString(actual)).append("\n");
                trusted = Arrays.copyOf(actual, actual.length);
                return;
            }

            if (Arrays.equals(trusted, actual)) return;
            expected = Arrays.copyOf(trusted, trusted.length);
        }

        certificateRejectionHandlers.forEach(handler -> handler.handle(expected, actual));
        throw new CertificateException("protoweaver-client-cert-error:" + hostId + ":" + StringUtil.toHexString(trusted) + "!=" + StringUtil.toHexString(actual));
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return EmptyArrays.EMPTY_X509_CERTIFICATES;
    }
}