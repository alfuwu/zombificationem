package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieMod;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AbstractZombieModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightArm;

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
    private void zombieArms(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (ZombieMod.ZOMBIE.get(livingEntity).isZombified() && !((BipedEntityModel<T>) (Object) this instanceof PlayerEntityModel<T>) && !((BipedEntityModel) (Object) this instanceof AbstractZombieModel))
            CrossbowPosing.meleeAttack(this.leftArm, this.rightArm, false, livingEntity.handSwingProgress, h);
    }
}
