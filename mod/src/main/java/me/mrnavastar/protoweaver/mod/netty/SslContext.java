package me.mrnavastar.protoweaver.mod.netty;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class SslContext {

    public static io.netty.handler.ssl.SslContext context;

    public static void init() {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate("localhost");
            context = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch (CertificateException | SSLException e) {
            e.printStackTrace();
        }
    }
}