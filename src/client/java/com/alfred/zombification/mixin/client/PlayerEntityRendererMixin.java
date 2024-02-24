package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieMod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("unused")
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @WrapOperation(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private void shakingArm(ModelPart part, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, Operation<Void> original, @Local VertexConsumerProvider provider, @Local AbstractClientPlayerEntity player, @Local Identifier skinTexture) {
        if (this.isShaking(player))
            matrices.translate(Math.cos((double) player.age * 3.25) * Math.PI * 0.0025, 0, Math.sin((double) player.age * 3.25) * Math.PI * 0.00125);
        if (ZombieMod.ZOMBIE.get(player).isZombified())
            part.render(matrices, provider.getBuffer(RenderLayer.getEntityTranslucent(skinTexture)), light, overlay, 0.69f, 1.0f, 0.69f, 1.0f);
        else
            original.call(part, matrices, vertices, light, overlay);
    }
}
