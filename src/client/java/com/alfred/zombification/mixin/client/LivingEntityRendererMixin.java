package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieMod;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @Inject(method = "isShaking", at = @At("HEAD"), cancellable = true)
    private void conversionShake(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (ZombieMod.ZOMBIE.get(entity).isUnzombifying() && ZombieMod.ZOMBIE.get(entity).isZombified())
            cir.setReturnValue(true);
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void modifyRenderColor(Args args, @Local T entity) {
        if (ZombieMod.ZOMBIE.get(entity).isZombified()) {
            args.set(4, 0.69f); // Red
            args.set(5, 1.0f); // Green
            args.set(6, 0.69f); // Blue
        }
    }
}
