package com.alfred.zombification.mixin;

import com.alfred.zombification.ZombieMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends LivingEntity {
    protected VillagerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow protected abstract void sayNo();

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void zombiesCantTalk(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!this.getWorld().isClient && ZombieMod.ZOMBIE.get(player).isZombified()) {
            this.sayNo();
            player.incrementStat(Stats.TALKED_TO_VILLAGER);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
