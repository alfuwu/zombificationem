package com.alfred.zombification.mixin;

import com.alfred.zombification.PlayerData;
import com.alfred.zombification.ZombieMod;
import com.alfred.zombification.access.ZombifiableEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements ZombifiableEntity {
	@Unique private boolean zombified = false;
	@Unique private boolean unzombifying = false;
	@Unique private int conversionTimer = PlayerData.BASE_CONVERSION_DELAY;

	protected ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "die", at = @At("HEAD"), cancellable = true)
	private void onDeath(DamageSource damageSource, CallbackInfo ci) {
		if (this.getLastHurtByMob() instanceof Zombie && !this.isZombified()) {
			super.die(damageSource);
			ci.cancel();
		}
	}

	@Override
	public boolean isZombified() {
		return this.zombified;
	}

	@Override
	public void setZombified(boolean zombified) {
		this.zombified = zombified;
		ZombieMod.sendToAll((ServerPlayer) (Object) this);
	}

	@Override
	public boolean isUnzombifying() {
		return this.unzombifying;
	}

	@Override
	public void setUnzombifying(boolean unzombifying) {
		this.unzombifying = unzombifying;
		ZombieMod.sendToAll((ServerPlayer) (Object) this);
	}

	@Override
	public int getConversionTimer() {
		return this.conversionTimer;
	}

	@Override
	public void setConversionTimer(int conversionTimer) {
		this.conversionTimer = conversionTimer;
		ZombieMod.sendToAll((ServerPlayer) (Object) this);
	}

	@Override
	public void conversionTimerTick(int i) {
		this.conversionTimer -= i;
		ZombieMod.sendToAll((ServerPlayer) (Object) this);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
	private void readNbt(CompoundTag nbt, CallbackInfo ci) {
		if (nbt.contains("zombified"))
			this.setZombified(nbt.getBoolean("zombified"));
		if (nbt.contains("unzombifying"))
			this.setUnzombifying(nbt.getBoolean("unzombifying"));
		if (nbt.contains("conversionTimer"))
			this.setConversionTimer(nbt.getInt("conversionTimer"));
	}

	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	private void addNbt(CompoundTag nbt, CallbackInfo ci) {
		nbt.putBoolean("zombified", this.zombified);
		nbt.putBoolean("unzombifying", this.unzombifying);
		if (this.unzombifying)
			nbt.putInt("conversionTimer", this.conversionTimer);
	}
}