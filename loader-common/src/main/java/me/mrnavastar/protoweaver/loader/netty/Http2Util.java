package me.mrnavastar.protoweaver.loader.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;

public class Http2Util {

    public static ApplicationProtocolNegotiationHandler getAPNHandler() {
        return new ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_2) {

            @Override
            protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                    ctx.pipeline()
                        .addLast(Http2FrameCodecBuilder.forServer()
                            .build(), new Http2ServerResponseHandler());
                    return;
                }
                throw new IllegalStateException("Protocol: " + protocol + " not supported");
            }
        };

    }
}