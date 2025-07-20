package com.alfred.zombification;

import com.alfred.zombification.access.ZombifiableEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public class PlayerData {
    public boolean zombified;
    public boolean unzombifying;
    public int conversionTimer;
    public static final int BASE_CONVERSION_DELAY = 3600;

    public PlayerData(boolean zombified, boolean unzombifying, int conversionTimer) {
        this.zombified = zombified;
        this.unzombifying = unzombifying;
        this.conversionTimer = conversionTimer;
    }

    public FriendlyByteBuf toBuf() {
        return toBuf(this.zombified, this.unzombifying, this.conversionTimer);
    }

    public static FriendlyByteBuf toBuf(boolean zombified, boolean unzombifying, int conversionTimer) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.copyBoolean(zombified, unzombifying));
        if (unzombifying)
            packet.writeInt(conversionTimer);
        return packet;
    }

    public static FriendlyByteBuf toBuf(ZombifiableEntity maybeZombie) {
        return toBuf(maybeZombie.isZombified(), maybeZombie.isUnzombifying(), maybeZombie.getConversionTimer());
    }

    public static PlayerData fromBuf(FriendlyByteBuf buf) {
        return buf.readableBytes() > 18 ?
                new PlayerData(buf.readBoolean(), buf.readBoolean(), buf.readInt()) :
                new PlayerData(buf.readBoolean(), buf.readBoolean(), BASE_CONVERSION_DELAY);
    }
}
