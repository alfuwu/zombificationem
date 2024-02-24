package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;

    @ModifyConstant(method = "handleInputEvents", constant = @Constant(intValue = 9))
    private int modifyKeys(int i) {
        return this.player != null && ZombieMod.ZOMBIE.get(this.player).isZombified() ? 1 : 9;
    }
}
