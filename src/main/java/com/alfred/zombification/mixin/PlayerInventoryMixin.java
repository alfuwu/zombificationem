package com.alfred.zombification.mixin;

import com.alfred.zombification.ZombieMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow @Final public PlayerEntity player;

    @Shadow @Final public DefaultedList<ItemStack> main;

    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    private void preventHotbarItemChange(double scrollAmount, CallbackInfo ci) {
        if (ZombieMod.ZOMBIE.get(this.player).isZombified())
            ci.cancel();
    }

    @Inject(method = "getEmptySlot", at = @At("HEAD"), cancellable = true)
    private void preventItemPickUp(CallbackInfoReturnable<Integer> cir) {
        if (ZombieMod.ZOMBIE.get(this.player).isZombified())
            cir.setReturnValue(this.main.get(0).isEmpty() ? 0 : -1);
    }
}
