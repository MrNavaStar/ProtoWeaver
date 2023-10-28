package me.mrnavastar.protoweaver.loader.netty;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

@UtilityClass
public class SSLContext {

    public static io.netty.handler.ssl.SslContext context;

    @SneakyThrows
    public static void init() {
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
    }

    @SneakyThrows
    public static void genKeys() {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
    }
}