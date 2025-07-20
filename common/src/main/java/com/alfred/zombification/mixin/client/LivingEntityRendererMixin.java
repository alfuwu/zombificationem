package com.alfred.zombification.mixin.client;

import com.alfred.zombification.access.ZombifiableEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @Inject(method = "isShaking", at = @At("HEAD"), cancellable = true)
    private void conversionShake(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ZombifiableEntity zomb && zomb.isUnzombifying() && zomb.isZombified())
            cir.setReturnValue(true);
    }

    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void modifyRenderColor(EntityModel<?> instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float a, T entity, float l, float m, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int k) {
        if (entity instanceof ZombifiableEntity zomb && zomb.isZombified())
            instance.renderToBuffer(poseStack, vertexConsumer, i, j, f*0.69f, g, h*0.69f, a);
        else
            instance.renderToBuffer(poseStack, vertexConsumer, i, j, f, g, h, a);
    }
}
