package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantCompatibilityMixin {

    @Inject(method = "areCompatible", at = @At("HEAD"), cancellable = true)
    private static void allowMendingWithInfinity(
            Holder<Enchantment> first, Holder<Enchantment> second,
            CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mendingInfinityEnabled) return;
        boolean firstMending = first.is(Enchantments.MENDING);
        boolean secondMending = second.is(Enchantments.MENDING);
        boolean firstInfinity = first.is(Enchantments.INFINITY);
        boolean secondInfinity = second.is(Enchantments.INFINITY);
        if ((firstMending && secondInfinity) || (firstInfinity && secondMending)) {
            cir.setReturnValue(true);
        }
    }
}
