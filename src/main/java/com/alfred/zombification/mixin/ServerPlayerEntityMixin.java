package com.alfred.zombification.mixin;

import com.alfred.zombification.ZombieMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin {
	protected ServerPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
	private void onDeath(DamageSource damageSource, CallbackInfo ci) {
		if (this.getPrimeAdversary() instanceof ZombieEntity && !ZombieMod.ZOMBIE.get(this).isZombified()) {
			super.onDeath(damageSource);
			ci.cancel();
		}
	}
}