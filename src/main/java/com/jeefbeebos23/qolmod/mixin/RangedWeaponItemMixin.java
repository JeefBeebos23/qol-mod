package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes Infinity-enchanted bows not consume any arrow type (regular, tipped, spectral).
 *
 * Injects into useAmmo() and, when the weapon has Infinity, returns a copy of the
 * ammo stack with INTANGIBLE_PROJECTILE set so the arrow entity is picked back up.
 */
@Mixin(ProjectileWeaponItem.class)
public class RangedWeaponItemMixin {

    @Inject(
        method = "useAmmo",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void infinityAllArrows(
            ItemStack weapon, ItemStack ammo, LivingEntity shooter, boolean multishot,
            CallbackInfoReturnable<ItemStack> cir) {

        if (!QolConfig.getInstance().infinityBowEnabled) return;
        if (multishot) return;
        if (shooter instanceof Player player && player.isCreative()) return;

        ItemEnchantments enchantments = weapon.getOrDefault(
            DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        for (Holder<Enchantment> entry : enchantments.keySet()) {
            if (entry.is(Enchantments.INFINITY)) {
                ItemStack intangible = ammo.copyWithCount(1);
                intangible.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
                cir.setReturnValue(intangible);
                return;
            }
        }
    }
}
