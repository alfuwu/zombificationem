package com.alfred.zombification.mixin;

import com.alfred.zombification.PlayerData;
import com.alfred.zombification.ZombieMod;
import com.alfred.zombification.access.ZombifiableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract void broadcastBreakEvent(EquipmentSlot slot);
    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot slot);
    @Shadow @Nullable public abstract AttributeInstance getAttribute(Attribute attribute);
    @Shadow protected abstract float getSoundVolume();
    @Shadow public abstract float getVoicePitch();
    @Shadow public abstract boolean hasEffect(MobEffect effect);
    @Unique private static final int MINIMUM_SOUND_DELAY = -80;
    @Unique private int ambientSoundChance = MINIMUM_SOUND_DELAY;

    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Unique
    private int getConversionRate() {
        int i = 1;
        if (this.random.nextFloat() < 0.01F) {
            int j = 0;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int k = (int) this.getX() - 4; k < (int) this.getX() + 4 && j < 14; ++k) {
                for (int l = (int) this.getY() - 4; l < (int) this.getY() + 4 && j < 14; ++l) {
                    for (int m = (int) this.getZ() - 4; m < (int) this.getZ() + 4 && j < 14; ++m) {
                        BlockState blockState = this.getLevel().getBlockState(mutable.set(k, l, m));
                        if (blockState.is(Blocks.IRON_BARS) || blockState.getBlock() instanceof BedBlock) {
                            if (this.random.nextFloat() < 0.3F)
                                ++i;
                            ++j;
                        }
                    }
                }
            }
        }

        return i;
    }

    @Unique
    protected boolean isAffectedByDaylight() {
        if (this.getLevel().isDay() && !this.getLevel().isClientSide) {
            float f = this.getBrightness();
            BlockPos blockPos = new BlockPos(Math.floor(this.getX()), Math.floor(this.getEyeY()), Math.floor(this.getZ()));
            boolean bl = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
            return f > 0.5f && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && !bl && this.getLevel().canSeeSky(blockPos);
        }
        return false;
    }

    @Inject(method = "getMobType", at = @At("HEAD"), cancellable = true)
    public void getMobType(CallbackInfoReturnable<MobType> cir) {
        if (this instanceof ZombifiableEntity zomb && zomb.isZombified())
            cir.setReturnValue(MobType.UNDEAD);
    }

    @Inject(method = "eat", at = @At("HEAD"))
    private void startUnzombify(Level world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (this instanceof ZombifiableEntity zomb && zomb.isZombified() && !zomb.isUnzombifying() && this.hasEffect(MobEffects.WEAKNESS) && stack.getItem() == Items.GOLDEN_APPLE) {
            zomb.setUnzombifying(true);
            zomb.setConversionTimer(PlayerData.BASE_CONVERSION_DELAY + random.nextInt(2401));
            this.playSound(SoundEvents.ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
        }
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void noTargetZombs(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof ZombifiableEntity zomb && zomb.isZombified() && (LivingEntity) (Object) this instanceof Monster)
            cir.setReturnValue(false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void zombieTick(CallbackInfo ci) {
        if (this instanceof ZombifiableEntity zomb && zomb.isZombified()) {
            if (this.isAlive() && (LivingEntity) (Object) this instanceof Player && this.random.nextInt(1000) < this.ambientSoundChance++) {
                this.ambientSoundChance = MINIMUM_SOUND_DELAY;
                this.playSound(SoundEvents.ZOMBIE_AMBIENT, this.getSoundVolume(), this.getVoicePitch());
            }

            if (zomb.isUnzombifying()) {
                int i = this.getConversionRate();
                zomb.conversionTimerTick(i);
                if (zomb.getConversionTimer() <= 0) {
                    zomb.setUnzombifying(false);
                    zomb.setZombified(false);
                    zomb.setConversionTimer(-1);
                    AttributeInstance moveSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (moveSpeed != null)
                        moveSpeed.removeModifier(ZombieMod.ZOMBIE_SPEED_MODIFIER);
                }
            }

            if (this.isAffectedByDaylight()) {
                ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
                if (!itemStack.isEmpty()) {
                    if (itemStack.isDamageableItem()) {
                        itemStack.setDamageValue(itemStack.getDamageValue() + this.random.nextInt(2));
                        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                            this.broadcastBreakEvent(EquipmentSlot.HEAD);
                            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }
                } else {
                    this.setSecondsOnFire(8);
                }
            }
        }
    }
}
