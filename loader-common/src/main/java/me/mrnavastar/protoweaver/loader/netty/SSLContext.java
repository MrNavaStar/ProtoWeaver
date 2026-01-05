package me.mrnavastar.protoweaver.loader.netty;

import io.netty.handler.ssl.*;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class SSLContext {

    @Getter
    private static io.netty.handler.ssl.SslContext context;
    private static InputStream privateKey;
    private static InputStream cert;
    private static final Provider provider = new BouncyCastleProvider();

    // These https://wiki.mozilla.org/Security/Server_Side_TLS#Intermediate_compatibility_.28recommended.29
    // Minus These https://datatracker.ietf.org/doc/html/rfc7540#appendix-A
    private static final List<String> CIPHERS = List.of(
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
            "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
            "TLS_AES_128_GCM_SHA256",
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256"
    );

    @SneakyThrows
    public static void init(String dir) {
        Security.addProvider(provider);

        Optional.ofNullable(System.getenv("PROTOWEAVER_PRIVATE_KEY")).ifPresent(value -> privateKey = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
        Optional.ofNullable(System.getenv("PROTOWEAVER_CERT")).ifPresent(value -> cert = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));

        genKeys(dir);
        context = SslContextBuilder.forServer(cert, privateKey)
                .sslProvider(OpenSsl.isAvailable() ? SslProvider.OPENSSL : SslProvider.JDK)
                .ciphers(CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2)
                ).build();
    }

    private static void genKeys(String dir) throws NoSuchAlgorithmException, CertificateException, IOException, OperatorCreationException {
        if (privateKey != null && cert != null) return;

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        X509Certificate certificate = genCert(kp);

        File privateKeyFile = new File(dir + "/keys/private.pem");
        File certFile = new File(dir + "/keys/cert.pem");

        if (!privateKeyFile.exists() || !certFile.exists()) {
            ProtoLogger.info("Generating SSL Keys");
            privateKeyFile.getParentFile().mkdirs();

            @Cleanup JcaPEMWriter privateWriter = new JcaPEMWriter(new FileWriter(privateKeyFile));
            @Cleanup JcaPEMWriter certWriter = new JcaPEMWriter(new FileWriter(certFile));
            privateWriter.writeObject(kp.getPrivate());
            certWriter.writeObject(certificate);
        }

        privateKey = new FileInputStream(privateKeyFile);
        cert = new FileInputStream(certFile);
    }

    // From https://stackoverflow.com/questions/29852290/self-signed-x509-certificate-with-bouncy-castle-in-java
    private static X509Certificate genCert(KeyPair keyPair) throws OperatorCreationException, CertificateException, IOException {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 999);
        Date endDate = calendar.getTime();

        X500Name dnName = new X500Name("CN=PROTOWEAVER");
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, new BigInteger(Long.toString(now)), startDate, endDate, dnName, keyPair.getPublic());
        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, new BasicConstraints(true));

        return new JcaX509CertificateConverter().setProvider(provider).getCertificate(certBuilder.build(contentSigner));
    }
}
