package me.mrnavastar.singularity.velocity.networking;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public class CustomWrapperPlayServerOpenWindow extends PacketWrapper<CustomWrapperPlayServerOpenWindow> {
    @Getter
    private int containerId;
    @Getter
    private int type;
    @Getter
    private String legacyType;
    @Getter
    private int legacySlots;
    @Getter
    private int horseId;
    @Getter
    private String title;
    private Component titleAsComponent = null;
    @Getter
    private boolean useProvidedWindowTitle;

    public CustomWrapperPlayServerOpenWindow(PacketSendEvent event) {
        super(event);
    }

    public void read() {
        if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
            this.containerId = this.readUnsignedByte();
        } else {
            this.containerId = this.readVarInt();
        }

        if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
            this.type = this.readUnsignedByte();
            this.title = this.readString(32);
            this.legacySlots = this.readUnsignedByte();
            this.useProvidedWindowTitle = this.readBoolean();
            if (this.type == 11) {
                this.horseId = this.readInt();
            }

        } else {
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14)) {
                this.type = this.readVarInt();
                this.title = this.readComponentJSON();
            } else {
                this.legacyType = this.readString();
                this.title = this.readComponentJSON();
                this.legacySlots = this.readUnsignedByte();
                if (this.legacyType.equals("EntityHorse")) {
                    this.horseId = this.readInt();
                }
            }

        }
    }

    public void write() {
        if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
            this.writeByte(this.containerId);
        } else {
            this.writeVarInt(this.containerId);
        }

        if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
            this.writeByte(this.type);
            this.writeString(this.title);
            this.writeByte(this.legacySlots);
            this.writeBoolean(this.useProvidedWindowTitle);
            if (this.type == 11) {
                this.writeInt(this.horseId);
            }

        } else {
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14)) {
                this.writeVarInt(this.type);
                if (this.titleAsComponent != null) {
                    this.writeComponent(this.titleAsComponent);
                } else {
                    this.writeString(this.title);
                }
            } else {
                this.writeString(this.legacyType);
                if (this.titleAsComponent != null) {
                    this.writeComponent(this.titleAsComponent);
                } else {
                    this.writeString(this.title);
                }

                this.writeByte(this.legacySlots);
                if (this.legacyType.equals("EntityHorse")) {
                    this.writeInt(this.horseId);
                }
            }

        }
    }
}