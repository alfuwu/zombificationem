package com.alfred.zombification.mixin;

import com.alfred.zombification.ZombieMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ActiveTargetGoal.class)
public abstract class ActiveTargetGoalMixin extends TrackTargetGoal {
    @Shadow protected TargetPredicate targetPredicate;
    @Shadow protected abstract Box getSearchBox(double distance);
    @Shadow @Final protected Class<? extends LivingEntity> targetClass;
    @Shadow @Nullable protected LivingEntity targetEntity;

    public ActiveTargetGoalMixin(MobEntity mob, boolean checkVisibility) {
        super(mob, checkVisibility);
    }

    @Override
    public boolean shouldContinue() {
        boolean bl = this.target == null || ZombieMod.ZOMBIE.get(this.target).isZombified();
        return super.shouldContinue() && (!bl || this.targetClass == MobEntity.class || MobEntity.class.isAssignableFrom(this.targetClass));
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/MobEntity;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V", at = @At("TAIL"))
    private void modifyPredicate(MobEntity mob, Class<? extends LivingEntity> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate, CallbackInfo ci) {
        if (targetClass == MobEntity.class || MobEntity.class.isAssignableFrom(targetClass)) {
            Predicate<LivingEntity> zombificationPredicate = entity -> ZombieMod.ZOMBIE.get(entity).isZombified();
            if (targetPredicate != null)
                targetPredicate = targetPredicate.or(zombificationPredicate);
            else
                targetPredicate = zombificationPredicate;
        } else {
            Predicate<LivingEntity> zombificationPredicate = entity -> !ZombieMod.ZOMBIE.get(entity).isZombified();
            if (targetPredicate != null)
                targetPredicate = targetPredicate.and(zombificationPredicate);
            else
                targetPredicate = zombificationPredicate;
        }
        this.targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(targetPredicate);
    }

    @Inject(method = "findClosestTarget", at = @At("RETURN"))
    private void changeTarget(CallbackInfo ci) {
        if (this.targetClass == MobEntity.class || MobEntity.class.isAssignableFrom(this.targetClass)) {
            LivingEntity closerEntity = this.mob.getWorld().getClosestEntity(this.mob.getWorld().getEntitiesByClass(LivingEntity.class, this.getSearchBox(this.getFollowRange()), livingEntity -> ZombieMod.ZOMBIE.get(livingEntity).isZombified()),
                    TargetPredicate.DEFAULT, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            if (this.targetEntity == null || (closerEntity != null && closerEntity.squaredDistanceTo(this.mob) < this.targetEntity.squaredDistanceTo(this.mob)))
                this.targetEntity = closerEntity;
        }
    }
}
