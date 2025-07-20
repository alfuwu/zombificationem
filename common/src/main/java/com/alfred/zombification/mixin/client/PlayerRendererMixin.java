package com.alfred.zombification.mixin.client;

import com.alfred.zombification.access.ZombifiableEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerRendererMixin(EntityRendererProvider.Context ctx, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void shakingArm(ModelPart part, PoseStack matrices, VertexConsumer vertices, int light, int overlay, PoseStack poseStack, MultiBufferSource provider, int i, AbstractClientPlayer player, ModelPart modelPart, ModelPart modelPart2) {
        if (this.isShaking(player))
            matrices.translate(Math.cos((double) player.tickCount * 3.25) * Math.PI * 0.0025, 0, Math.sin((double) player.tickCount * 3.25) * Math.PI * 0.00125);
        if (((ZombifiableEntity) player).isZombified())
            part.render(matrices, vertices, light, overlay, 0.69f, 1.0f, 0.69f, 1.0f);
        else
            part.render(matrices, vertices, light, overlay);
    }
}
