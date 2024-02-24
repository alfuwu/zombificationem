package com.alfred.zombification.mixin;

import com.alfred.zombification.ZombieMod;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public abstract void incrementStat(Stat<?> stat);

    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Unique private static final UUID ZOMBIE_SPEED_MODIFIER = UUID.fromString("121C953C-A264-423A-B6fB-C51FC5C040B9");

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
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
            return f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !bl && this.getWorld().isSkyVisible(blockPos);
        }
        return false;
    }


    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity livingEntity = this.getPrimeAdversary();
        System.out.println("player: " + livingEntity);
        if (livingEntity instanceof ZombieEntity && !ZombieMod.ZOMBIE.get(this).isZombified()) {
            ci.cancel();
            this.incrementStat(Stats.KILLED_BY.getOrCreateStat(livingEntity.getType()));
            livingEntity.updateKilledAdvancementCriterion(this, this.scoreAmount, damageSource);
            ZombieMod.ZOMBIE.get(this).setZombified(true);
            this.setHealth(this.getMaxHealth());
            this.dead = false;
            EntityAttributeInstance moveSpeed = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (moveSpeed != null) {
                moveSpeed.removeModifier(ZOMBIE_SPEED_MODIFIER);
                moveSpeed.addPersistentModifier(
                    new EntityAttributeModifier(ZOMBIE_SPEED_MODIFIER, "Zombie speed modifier", -0.2f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }
        }
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
            if (ZombieMod.ZOMBIE.get(this).isUnzombifying()) {
                int i = this.getConversionRate();
                ZombieMod.ZOMBIE.get(this).conversionTimerTick(i);
                if (ZombieMod.ZOMBIE.get(this).getConversionTimer() <= 0) {
                    ZombieMod.ZOMBIE.get(this).setUnzombifying(false);
                    ZombieMod.ZOMBIE.get(this).setZombified(false);
                    ZombieMod.ZOMBIE.get(this).setConversionTimer(-1);
                    EntityAttributeInstance moveSpeed = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    if (moveSpeed != null)
                        moveSpeed.removeModifier(ZOMBIE_SPEED_MODIFIER);
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
