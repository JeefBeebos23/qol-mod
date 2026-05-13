package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
        method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private <T extends LivingEntity> void preventElytraDamage(
            int amount, T entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack self = (ItemStack)(Object)this;
        if (QolConfig.getInstance().elytraDurabilityEnabled
                && self.isOf(Items.ELYTRA)
                && slot == EquipmentSlot.CHEST) {
            ci.cancel();
        }
    }
}
