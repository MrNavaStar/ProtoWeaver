package me.mrnavastar.protoweaver.loader.netty;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.experimental.UtilityClass;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

@UtilityClass
public class SSLContext {

    public static io.netty.handler.ssl.SslContext context;

    public static void init() {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            context = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                .sslProvider(OpenSsl.isAvailable() ? SslProvider.OPENSSL : SslProvider.JDK)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,

                        //ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1))
                .build();
        } catch (CertificateException | SSLException e) {
            e.printStackTrace();
        }
    }
}