package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieClient;
import com.alfred.zombification.access.ZombifiableEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow @Nullable public LocalPlayer player;

    @ModifyConstant(method = "handleKeybinds", constant = @Constant(intValue = 9))
    private int modifyKeys(int i) {
        return this.player != null && ((ZombifiableEntity) this.player).isZombified() ? 1 : i;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        ZombieClient.tick((Minecraft) (Object) this);
    }
}
