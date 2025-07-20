package com.alfred.zombification.mixin;

import com.alfred.zombification.access.ZombifiableEntity;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public abstract class VillagerMixin extends LivingEntity {
    protected VillagerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract void setUnhappy();

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void zombiesCantTalk(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!this.getLevel().isClientSide && ((ZombifiableEntity) player).isZombified()) {
            this.setUnhappy();
            player.awardStat(Stats.TALKED_TO_VILLAGER);
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
