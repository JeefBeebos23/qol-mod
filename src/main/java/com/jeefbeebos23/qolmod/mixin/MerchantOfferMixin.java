package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.library.LibraryItems;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantOffer.class)
public class MerchantOfferMixin {

    @Inject(method = "isOutOfStock", at = @At("HEAD"), cancellable = true)
    private void neverOutOfStock(CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().noVillagerRestock) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "resetUses", at = @At("HEAD"), cancellable = true)
    private void preventMysteryBookRestock(CallbackInfo ci) {
        if (LibraryItems.MYSTERY_BOOK != null
                && ((MerchantOffer)(Object)this).getResult().is(LibraryItems.MYSTERY_BOOK)) {
            ci.cancel();
        }
    }
}
