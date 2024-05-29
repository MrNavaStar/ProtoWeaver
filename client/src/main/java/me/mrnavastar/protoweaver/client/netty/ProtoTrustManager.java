package me.mrnavastar.protoweaver.client.netty;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.StringUtil;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * This trust manager is based off the {@link io.netty.handler.ssl.util.FingerprintTrustManagerFactory} but modified to
 * function in the manner that ssh does (By always trusting the first connection and saving its fingerprint).
 */
public class ProtoTrustManager {

    private final File hostsFile;
    private static final FastThreadLocal<MessageDigest> tlmd;

    static {
        tlmd = new FastThreadLocal<>() {

            @Override
            protected MessageDigest initialValue() {
                try {
                    return MessageDigest.getInstance("SHA256");
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalArgumentException("Unsupported hash algorithm", e);
                }
            }
        };
    }

    private final String hostId;
    private byte[] trusted = null;
    @Getter
    private final TrustManager tm = new X509TrustManager() {

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
            MessageDigest md = tlmd.get();
            md.reset();
            byte[] fingerprint = md.digest(chain[0].getEncoded());

            if (trusted == null) {
                hostsFile.getParentFile().mkdirs();
                hostsFile.createNewFile();

                @Cleanup BufferedWriter writer = new BufferedWriter(new FileWriter(hostsFile, true));
                writer.append(hostId).append("=").append(StringUtil.toHexString(fingerprint)).append("\n");
                trusted = fingerprint;
                return;
            }

            if (Arrays.equals(trusted, fingerprint)) return;
            throw new CertificateException("protoweaver-client-cert-error:" + hostId + ":" + StringUtil.toHexString(trusted) + "!=" + StringUtil.toHexString(fingerprint));
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return EmptyArrays.EMPTY_X509_CERTIFICATES;
        }
    };

    public ProtoTrustManager(String host, int port, String file) {
        hostsFile = new File(file + File.separator + "protoweaver.hosts");
        this.hostId = host + ":" + port;
        if (!hostsFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(hostsFile))) {
            reader.lines().filter(l -> l.startsWith(host + ":" + port)).findFirst()
                    .ifPresent(l -> trusted = StringUtil.decodeHexDump(l.split("=")[1]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}