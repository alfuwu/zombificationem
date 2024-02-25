package com.alfred.zombification.mixin;

import com.alfred.zombification.ZombieMod;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    @Shadow public abstract void sendEquipmentBreakStatus(EquipmentSlot slot);
    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);
    @Shadow @Nullable public abstract EntityAttributeInstance getAttributeInstance(EntityAttribute attribute);
    @Shadow protected abstract float getSoundVolume();
    @Shadow public abstract float getSoundPitch();
    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);
    @Unique private static final int MINIMUM_SOUND_DELAY = -80;
    @Unique private int ambientSoundChance = MINIMUM_SOUND_DELAY;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private int getConversionRate() {
        int i = 1;
        if (this.random.nextFloat() < 0.01F) {
            int j = 0;
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int k = (int) this.getX() - 4; k < (int) this.getX() + 4 && j < 14; ++k) {
                for (int l = (int) this.getY() - 4; l < (int) this.getY() + 4 && j < 14; ++l) {
                    for (int m = (int) this.getZ() - 4; m < (int) this.getZ() + 4 && j < 14; ++m) {
                        BlockState blockState = this.getWorld().getBlockState(mutable.set(k, l, m));
                        if (blockState.isOf(Blocks.IRON_BARS) || blockState.getBlock() instanceof BedBlock) {
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
        if (this.getWorld().isDay() && !this.getWorld().isClient) {
            float f = this.getBrightnessAtEyes();
            BlockPos blockPos = BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ());
            boolean bl = this.isWet() || this.inPowderSnow || this.wasInPowderSnow;
            return f > 0.5f && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && !bl && this.getWorld().isSkyVisible(blockPos);
        }
        return false;
    }

    @Inject(method = "getGroup", at = @At("HEAD"), cancellable = true)
    public void getGroup(CallbackInfoReturnable<EntityGroup> cir) {
        if (ZombieMod.ZOMBIE.get(this).isZombified())
            cir.setReturnValue(EntityGroup.UNDEAD);
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void startUnzombify(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (ZombieMod.ZOMBIE.get(this).isZombified() && !ZombieMod.ZOMBIE.get(this).isUnzombifying() && this.hasStatusEffect(StatusEffects.WEAKNESS) && stack.getItem() == Items.GOLDEN_APPLE) {
            ZombieMod.ZOMBIE.get(this).setUnzombifying(true);
            ZombieMod.ZOMBIE.get(this).setConversionTimer();
            this.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void zombieTick(CallbackInfo ci) {
        if (ZombieMod.ZOMBIE.get(this).isZombified()) {
            if (this.isAlive() && (LivingEntity) (Object) this instanceof PlayerEntity && this.random.nextInt(1000) < this.ambientSoundChance++) {
                this.ambientSoundChance = MINIMUM_SOUND_DELAY;
                this.playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, this.getSoundVolume(), this.getSoundPitch());
            }

            if (ZombieMod.ZOMBIE.get(this).isUnzombifying()) {
                int i = this.getConversionRate();
                ZombieMod.ZOMBIE.get(this).conversionTimerTick(i);
                if (ZombieMod.ZOMBIE.get(this).getConversionTimer() <= 0) {
                    ZombieMod.ZOMBIE.get(this).setUnzombifying(false);
                    ZombieMod.ZOMBIE.get(this).setZombified(false);
                    ZombieMod.ZOMBIE.get(this).setConversionTimer(-1);
                    EntityAttributeInstance moveSpeed = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    if (moveSpeed != null)
                        moveSpeed.removeModifier(ZombieMod.ZOMBIE_SPEED_MODIFIER);
                }
            }

            if (this.isAffectedByDaylight()) {
                ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
                if (!itemStack.isEmpty()) {
                    if (itemStack.isDamageable()) {
                        itemStack.setDamage(itemStack.getDamage() + this.random.nextInt(2));
                        if (itemStack.getDamage() >= itemStack.getMaxDamage()) {
                            this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                            this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }
                } else {
                    this.setOnFireFor(8);
                }
            }
        }
    }
}
