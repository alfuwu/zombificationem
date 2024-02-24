package com.alfred.zombification.cca;

import com.alfred.zombification.ZombieMod;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class ZombificationComponent implements AutoSyncedComponent {
    private final LivingEntity self;
    private boolean zombified = false;
    private boolean unzombifying = false;
    private int conversionTimer = BASE_CONVERSION_DELAY;
    private static final int BASE_CONVERSION_DELAY = 3600;

    public ZombificationComponent(LivingEntity self) {
        this.self = self;
    }

    public boolean isZombified() {
        return this.zombified;
    }

    public boolean isUnzombifying() {
        return this.unzombifying;
    }

    public int getConversionTimer() {
        return this.conversionTimer;
    }

    public void setZombified(boolean bl) {
        this.zombified = bl;
        ZombieMod.ZOMBIE.sync(this.self);
    }

    public void setUnzombifying(boolean bl) {
        this.unzombifying = bl;
        ZombieMod.ZOMBIE.sync(this.self);
    }

    public void setConversionTimer() {
        this.setConversionTimer(BASE_CONVERSION_DELAY + self.getRandom().nextInt(2401));
    }

    public void setConversionTimer(int i) {
        this.conversionTimer = i;
    }

    public void conversionTimerTick(int i) {
        this.conversionTimer -= i;
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (nbt.contains("Zombified", NbtElement.BYTE_TYPE))
            this.setZombified(nbt.getBoolean("Zombified"));
        if (nbt.contains("ConversionTime", NbtElement.NUMBER_TYPE)) {
            int conversionTime = nbt.getInt("ConversionTime");
            if (conversionTime == -1) {
                this.setUnzombifying(false);
                this.conversionTimer = -1;
            } else {
                this.setUnzombifying(true);
                this.conversionTimer = conversionTime;
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        if (this.isZombified())
            nbt.putBoolean("Zombified", true);
        if (this.isUnzombifying())
            nbt.putInt("ConversionTime", conversionTimer);
    }
}
