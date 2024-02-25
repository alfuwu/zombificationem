package com.alfred.zombification.mixin;

import com.alfred.zombification.ZombieMod;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public abstract void incrementStat(Stat<?> stat);
    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);
    @Shadow public abstract PlayerAbilities getAbilities();
    @Shadow public abstract HungerManager getHungerManager();
    @Shadow @Final private PlayerInventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void noZombieSwimming(CallbackInfo ci) {
        if (ZombieMod.ZOMBIE.get(this).isZombified()) {
            this.setSwimming(false);
            ci.cancel();
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity livingEntity = this.getPrimeAdversary();
        if (livingEntity instanceof ZombieEntity && !ZombieMod.ZOMBIE.get(this).isZombified()) {
            ci.cancel();
            this.incrementStat(Stats.KILLED_BY.getOrCreateStat(livingEntity.getType()));
            livingEntity.updateKilledAdvancementCriterion(this, this.scoreAmount, damageSource);
            ZombieMod.ZOMBIE.get(this).setZombified(true);
            this.setHealth(this.getMaxHealth());
            this.dead = false;
            this.inventory.selectedSlot = 0;
            if ((PlayerEntity) (Object) this instanceof ServerPlayerEntity serverPlayer)
                ServerPlayNetworking.send(serverPlayer, ZombieMod.SELECT_SLOT, new PacketByteBuf(Unpooled.copyShort(0)));
            EntityAttributeInstance moveSpeed = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (moveSpeed != null) {
                moveSpeed.removeModifier(ZombieMod.ZOMBIE_SPEED_MODIFIER);
                moveSpeed.addPersistentModifier(
                    new EntityAttributeModifier(ZombieMod.ZOMBIE_SPEED_MODIFIER, "Zombie speed modifier", -0.2f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void nom(Entity target, CallbackInfo ci) {
        if (target instanceof VillagerEntity || target instanceof PlayerEntity)
            this.getHungerManager().add(1, 0.2f);
    }

    @Inject(method = "eatFood", at = @At("HEAD"), cancellable = true)
    private void modifyFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.getItem() == Items.ROTTEN_FLESH && ZombieMod.ZOMBIE.get(this).isZombified()) {
            // Eat without applying effects
            this.getHungerManager().eat(stack.getItem(), stack);
            this.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
            if ((PlayerEntity) (Object) this instanceof ServerPlayerEntity serverPlayer)
                Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
            if (stack.isFood()) {
                world.playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatSound(stack), SoundCategory.NEUTRAL, 1.0f, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.4f);
                if (!this.getAbilities().creativeMode)
                    stack.decrement(1);

                this.emitGameEvent(GameEvent.EAT);
            }
            cir.setReturnValue(stack);
        } else if (stack.getItem() != Items.GOLDEN_APPLE && stack.getItem().getFoodComponent() != null && !stack.getItem().getFoodComponent().getStatusEffects().stream().anyMatch(pair -> !pair.getFirst().getEffectType().isBeneficial())) {
            // If the food item had no bad status effects, apply hunger
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0));
        }
    }
}
