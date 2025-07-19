package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieClient;
import com.alfred.zombification.ZombieMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Nullable private ShaderEffect shader;
    @Shadow @Final private MinecraftClient client;
    @Shadow public abstract void loadShader(Identifier id);

    @Inject(method = "onCameraEntitySet", at = @At("RETURN"))
    private void modifyShader(Entity entity, CallbackInfo ci) {
        if (this.shader == null && ((entity instanceof LivingEntity livingEntity && ZombieMod.ZOMBIE.get(livingEntity).isZombified()) || entity instanceof ZombieEntity || entity instanceof ZombieHorseEntity || (entity == null && this.client.player != null && ZombieMod.ZOMBIE.get(this.client.player).isZombified())))
            this.loadShader(ZombieClient.ZOMBIE_VISION);
    }
}
