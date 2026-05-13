package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes Infinity-enchanted bows not consume any arrow type (regular, tipped, spectral).
 *
 * In vanilla, the Infinity enchantment's AMMO_USE effect is conditioned on the projectile
 * being Items.ARROW via a MatchToolLootCondition, so tipped and spectral arrows are still
 * consumed. We intercept getProjectile() before the ammo-use calculation and, when the
 * ranged weapon has Infinity, mark the arrow as INTANGIBLE_PROJECTILE (no consumption).
 */
@Mixin(RangedWeaponItem.class)
public class BowItemMixin {

    @Inject(
        method = "getProjectile",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void infinityAllArrows(
            ItemStack stack,
            ItemStack projectileStack,
            LivingEntity shooter,
            boolean multishot,
            CallbackInfoReturnable<ItemStack> cir) {

        // Only apply when the feature is enabled, not multishot, and not in creative
        // (creative mode already skips consumption via i == 0 path in vanilla)
        if (!QolConfig.getInstance().infinityBowEnabled) return;
        if (multishot) return;
        if (shooter.isInCreativeMode()) return;

        // Check if the ranged weapon has the Infinity enchantment
        ItemEnchantmentsComponent enchantments = stack.getOrDefault(
                DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        boolean hasInfinity = false;
        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            if (entry.matchesKey(Enchantments.INFINITY)) {
                hasInfinity = true;
                break;
            }
        }

        if (!hasInfinity) return;

        // Return a 1-count intangible copy — exactly what vanilla does when ammoUse == 0
        ItemStack intangible = projectileStack.copyWithCount(1);
        intangible.set(DataComponentTypes.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        cir.setReturnValue(intangible);
    }
}
