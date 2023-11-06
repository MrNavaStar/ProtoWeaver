package me.mrnavastar.protoweaver.client.netty;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.StringUtil;
import lombok.Cleanup;
import lombok.Getter;
import me.mrnavastar.protoweaver.core.util.DrunkenBishop;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * This trust manager is based off the {@link io.netty.handler.ssl.util.FingerprintTrustManagerFactory} but modified to
 * function in the manner that ssh does (By always trusting the first connection and saving its fingerprint).
 */
public class ProtoTrustManager {

    private static final File hostsFile = new File("./protoweaver_hosts");
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
        public void checkClientTrusted(X509Certificate[] chain, String s) throws CertificateException {
            checkTrusted(chain);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String s) throws CertificateException {
            checkTrusted(chain);
        }

        private void checkTrusted(X509Certificate[] chain) throws CertificateException {
            X509Certificate cert = chain[0];
            byte[] fingerprint = fingerprint(cert);
            if (trusted == null) {
                trusted = fingerprint;

                try {
                    @Cleanup BufferedWriter writer = new BufferedWriter(new FileWriter(hostsFile));
                    writer.append(hostId).append("=").append(StringUtil.toHexString(fingerprint));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Arrays.equals(trusted, fingerprint)) return;
            throw new CertificateException("protoweaver-client-cert-error:" + hostId + ":" + StringUtil.toHexString(trusted) + "!=" + StringUtil.toHexString(fingerprint));
        }

        private byte[] fingerprint(X509Certificate cert) throws CertificateEncodingException {
            MessageDigest md = tlmd.get();
            md.reset();
            return md.digest(cert.getEncoded());
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return EmptyArrays.EMPTY_X509_CERTIFICATES;
        }
    };

    public ProtoTrustManager(String host, int port) {
        this.hostId = host + ":" + port;
        if (!hostsFile.exists()) return;

        try {
            @Cleanup BufferedReader reader = new BufferedReader(new FileReader(hostsFile));
            for (String line : reader.lines().toArray(String[]::new)) {
                if (line.startsWith(hostId)) {
                    trusted = StringUtil.decodeHexDump(line.split("=")[1]);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}