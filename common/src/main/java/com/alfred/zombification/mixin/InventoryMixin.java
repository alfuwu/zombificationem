package com.alfred.zombification.mixin;

import com.alfred.zombification.access.ZombifiableEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Shadow @Final public Player player;
    @Shadow @Final public NonNullList<ItemStack> items;

    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void preventHotbarItemChange(double scrollAmount, CallbackInfo ci) {
        if (((ZombifiableEntity) this.player).isZombified())
            ci.cancel();
    }

    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void preventItemPickUp(CallbackInfoReturnable<Integer> cir) {
        if (((ZombifiableEntity) this.player).isZombified())
            cir.setReturnValue(this.items.get(0).isEmpty() ? 0 : -1);
    }
}
