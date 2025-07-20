package com.alfred.zombification.mixin.client;

import com.alfred.zombification.ZombieMod;
import com.alfred.zombification.access.ZombifiableEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 9))
    private int modifyItemAmount(int constant) {
        return this.minecraft.player != null && ((ZombifiableEntity) this.minecraft.player).isZombified() ? 1 : constant;
    }

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 90))
    private int modifyItemPosition(int constant) {
        return this.minecraft.player != null && ((ZombifiableEntity) this.minecraft.player).isZombified() ? 10 : constant;
    }

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 91))
    private int modifyHotbarHudPosition(int constant) {
        return this.minecraft.player != null && ((ZombifiableEntity) this.minecraft.player).isZombified() ? 11 : constant;
    }

    @ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 182))
    private int modifyHotbarSize(int constant) {
        return this.minecraft.player != null && ((ZombifiableEntity) this.minecraft.player).isZombified() ? 22 : constant;
    }
}
