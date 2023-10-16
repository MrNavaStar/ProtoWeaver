package me.mrnavastar.protoweaver.mod.mixin;

import io.netty.channel.ChannelPipeline;
import me.mrnavastar.protoweaver.mod.netty.ProtoDeterminer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.handler.PacketSizeLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Inject(method = "addHandlers", at = @At("HEAD"))
    private static void protoweaver_addCustomHandlers(ChannelPipeline pipeline, NetworkSide side, PacketSizeLogger packetSizeLogger, CallbackInfo ci) {
        pipeline.addAfter("timeout", "protoDeterminer", new ProtoDeterminer());
    }
}
