package me.mrnavastar.protoweaver.fabric.mixin;

import io.netty.channel.Channel;
import me.mrnavastar.protoweaver.loader.netty.ProtoDeterminer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/ServerNetworkIo$1")
public class ServerNetworkIoMixin {

    @Inject(method = "initChannel", at = @At("HEAD"))
    public void bind(Channel channel, CallbackInfo ci) {
        ProtoDeterminer.registerToPipeline(channel);
    }
}