package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private static Identifier HOTBAR_TEXTURE;
    @Unique private static final Identifier HOTBAR_TEXTURE_ONE_SLOT = ZombieMod.identifier("hotbar");

    @Inject(method = "renderHotbar", at = @At("HEAD"))
    private void modifyDraw(float tickDelta, DrawContext context, CallbackInfo ci) {
        // TODO: only draw the first item (& offhand) when in zombie mode
    }

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 9))
    private int modifyItemAmount(int constant) {
        return this.client.player != null && ZombieMod.ZOMBIE.get(this.client.player).isZombified() ? 1 : constant;
    }

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 90))
    private int modifyItemPosition(int constant) {
        return this.client.player != null && ZombieMod.ZOMBIE.get(this.client.player).isZombified() ? 10 : constant;
    }

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 91))
    private int modifyHotbarHudPosition(int constant) {
        return this.client.player != null && ZombieMod.ZOMBIE.get(this.client.player).isZombified() ? 11 : constant;
    }

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 182))
    private int modifyHotbarSize(int constant) {
        return this.client.player != null && ZombieMod.ZOMBIE.get(this.client.player).isZombified() ? 22 : constant;
    }

    @ModifyArg(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"), index = 0)
    private Identifier newHotbarTexture(Identifier texture) {
        return texture.equals(InGameHudMixin.HOTBAR_TEXTURE) && this.client.player != null && ZombieMod.ZOMBIE.get(this.client.player).isZombified() ? HOTBAR_TEXTURE_ONE_SLOT : texture;
    }
}
