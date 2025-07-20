package com.alfred.zombification.mixin.client;

import com.alfred.zombification.PlayerData;
import com.alfred.zombification.access.ZombifiableEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin implements ZombifiableEntity {
    @Unique private boolean zombified = false;
    @Unique private boolean unzombifying = false;
    @Unique private int conversionTimer = PlayerData.BASE_CONVERSION_DELAY;

    @Override
    public boolean isZombified() {
        return this.zombified;
    }

    @Override
    public void setZombified(boolean zombified) {
        this.zombified = zombified;
    }

    @Override
    public boolean isUnzombifying() {
        return this.unzombifying;
    }

    @Override
    public void setUnzombifying(boolean unzombifying) {
        this.unzombifying = unzombifying;
    }

    @Override
    public int getConversionTimer() {
        return this.conversionTimer;
    }

    @Override
    public void setConversionTimer(int conversionTimer) {
        this.conversionTimer = conversionTimer;
    }

    @Override
    public void conversionTimerTick(int i) {
        this.conversionTimer -= i;
    }
}
