package me.mrnavastar.protoweaver.loader.netty;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ServerChannelInitializer;
import io.netty.channel.Channel;

public class ProtoWeaverChannelInitializer extends ServerChannelInitializer {
    
    public ProtoWeaverChannelInitializer(VelocityServer server) {
        super(server);
    }

    @Override
    protected void initChannel(Channel ch) {
        super.initChannel(ch);
        ProtoDeterminer.registerToPipeline(ch);
    }
}
