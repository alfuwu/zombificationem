package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieMod;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
    @Shadow @Final public ModelPart leftSleeve;
    @Shadow @Final public ModelPart rightSleeve;

    public PlayerEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
    private void zombieArms(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (ZombieMod.ZOMBIE.get(livingEntity).isZombified()) {
            CrossbowPosing.meleeAttack(this.leftArm, this.rightArm, false, this.handSwingProgress, h);
            this.leftSleeve.copyTransform(this.leftArm);
            this.rightSleeve.copyTransform(this.rightArm);
            //CrossbowPosing.meleeAttack(this.leftSleeve, this.rightSleeve, false, this.handSwingProgress, h);
        }
    }
}
