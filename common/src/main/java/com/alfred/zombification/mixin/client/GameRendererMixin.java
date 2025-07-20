package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieClient;
import com.alfred.zombification.access.ZombifiableEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Nullable private PostChain postEffect;
    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract void loadEffect(ResourceLocation resourceLocation);

    @Inject(method = "checkEntityPostEffect", at = @At("RETURN"))
    private void modifyShader(Entity entity, CallbackInfo ci) {
        if (this.postEffect == null && ((entity instanceof ZombifiableEntity zomb && zomb.isZombified()) || entity instanceof Zombie || entity instanceof ZombieHorse || (entity == null && this.minecraft.player != null && ((ZombifiableEntity) this.minecraft.player).isZombified())))
            this.loadEffect(ZombieClient.ZOMBIE_VISION);
    }
}
