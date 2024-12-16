package me.mrnavastar.protoweaver.http;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.LocalConnector;

import java.util.function.Consumer;

public class ProtoJavalin {

    public static Javalin create(String endpoint) {
        return create(endpoint, (ignored) -> {});
    }

    public static Javalin create(String endpoint, Consumer<JavalinConfig> config) {
        return Javalin.create(javalinConfig -> {
            javalinConfig.showJavalinBanner = false;
            javalinConfig.useVirtualThreads = true;
            javalinConfig.jetty.addConnector((server, httpConfig) -> {
                LocalConnector connector = new LocalConnector(server, new HttpConnectionFactory(httpConfig));
                ProtoJetty.create(endpoint, connector);
                return connector;
            });
            config.accept(javalinConfig);
        });
    }
}