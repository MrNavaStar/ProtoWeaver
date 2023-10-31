package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import net.minecraftforge.fml.common.Mod;

@Mod(ProtoConstants.PROTOWEAVER_ID)
public class Forge {

    public Forge() {
        SSLContext.init();
    }
}