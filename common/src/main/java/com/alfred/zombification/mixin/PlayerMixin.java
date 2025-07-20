package com.alfred.zombification.mixin;

import com.alfred.zombification.ZombieMod;
import com.alfred.zombification.access.ZombifiableEntity;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements ZombifiableEntity {
    @Shadow public abstract void awardStat(Stat<?> stat);
    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);
    @Shadow public abstract Abilities getAbilities();
    @Shadow public abstract FoodData getFoodData();
    @Shadow @Final private Inventory inventory;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void noZombieSwimming(CallbackInfo ci) {
        if (this.isZombified()) {
            this.setSwimming(false);
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity livingEntity = this.getLastHurtByMob();
        if ((livingEntity instanceof Zombie || livingEntity instanceof ZombifiableEntity zomb && zomb.isZombified()) && !this.isZombified()) {
            ci.cancel();
            this.awardStat(Stats.ENTITY_KILLED_BY.get(livingEntity.getType()));
            livingEntity.awardKillScore(this, this.deathScore, damageSource);
            this.setZombified(true);
            this.setHealth(this.getMaxHealth());
            this.dead = false;
            this.inventory.selected = 0;
            if ((Player) (Object) this instanceof ServerPlayer serverPlayer)
                NetworkManager.sendToPlayer(serverPlayer, ZombieMod.SELECT_SLOT, new FriendlyByteBuf(Unpooled.copyShort(0)));
            AttributeInstance moveSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeed != null) {
                moveSpeed.removeModifier(ZombieMod.ZOMBIE_SPEED_MODIFIER);
                moveSpeed.addPermanentModifier(
                    new AttributeModifier(ZombieMod.ZOMBIE_SPEED_MODIFIER, "Zombie speed modifier", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void nom(Entity target, CallbackInfo ci) {
        if (this.isZombified() && (target instanceof Villager || target instanceof Player))
            this.getFoodData().eat(1, 0.2f);
    }

    @Inject(method = "eat", at = @At("HEAD"), cancellable = true)
    private void modifyFood(Level level, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (this.isZombified()) {
            if (stack.getItem() == Items.ROTTEN_FLESH) {
                // Eat without applying effects
                this.getFoodData().eat(stack.getItem(), stack);
                this.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f);
                if ((Player) (Object) this instanceof ServerPlayer serverPlayer)
                    CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
                if (stack.isEdible()) {
                    level.playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(stack), SoundSource.NEUTRAL, 1.0f, 1.0f + (level.random.nextFloat() - level.random.nextFloat()) * 0.4f);
                    if (!this.getAbilities().invulnerable)
                        stack.shrink(1);

                    this.gameEvent(GameEvent.EAT);
                }
                cir.setReturnValue(stack);
            } else if (stack.getItem() != Items.GOLDEN_APPLE && stack.getItem().getFoodProperties() != null && stack.getItem().getFoodProperties().getEffects().stream().allMatch(pair -> pair.getFirst().getEffect().isBeneficial())) {
                // If the food item had no bad status effects, apply hunger
                this.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));
            }
        }
    }

    @Inject(method = "killed", at = @At("HEAD"), cancellable = true)
    private void convertVillager(ServerLevel level, LivingEntity other, CallbackInfo ci) {
        if (this.isZombified()) {
            if ((level.getDifficulty() == Difficulty.NORMAL || level.getDifficulty() == Difficulty.HARD) && other instanceof Villager villagerEntity) {
                if (level.getDifficulty() != Difficulty.HARD && this.random.nextBoolean())
                    return;

                ZombieVillager zombieVillagerEntity = villagerEntity.convertTo(EntityType.ZOMBIE_VILLAGER, false);
                if (zombieVillagerEntity != null) {
                    zombieVillagerEntity.finalizeSpawn(level, level.getCurrentDifficultyAt(zombieVillagerEntity.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), null);
                    zombieVillagerEntity.setVillagerData(villagerEntity.getVillagerData());
                    zombieVillagerEntity.setGossips(villagerEntity.getGossips().store(NbtOps.INSTANCE).getValue());
                    zombieVillagerEntity.setTradeOffers(villagerEntity.getOffers().createTag());
                    zombieVillagerEntity.setVillagerXp(villagerEntity.getVillagerXp());
                    if (!this.isSilent())
                        level.globalLevelEvent(1026, this.blockPosition(), 0);

                    ci.cancel();
                }
            }
        }
    }
}
