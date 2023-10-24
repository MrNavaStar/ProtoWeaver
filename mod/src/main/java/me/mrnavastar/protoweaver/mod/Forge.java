package me.mrnavastar.protoweaver.mod;

import me.mrnavastar.protoweaver.mod.netty.SSLContext;
import me.mrnavastar.protoweaver.util.ProtoConstants;
import net.minecraftforge.fml.common.Mod;

@Mod(ProtoConstants.PROTOWEAVER_ID)
public class Forge {

    public Forge() {
        SSLContext.init();
    }
}