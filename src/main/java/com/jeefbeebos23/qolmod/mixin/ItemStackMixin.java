package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
        method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void preventElytraDamage(
            int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        if (QolConfig.getInstance().elytraDurabilityEnabled
                && ((ItemStack)(Object)this).is(Items.ELYTRA)
                && slot == EquipmentSlot.CHEST) {
            ci.cancel();
        }
    }
}
