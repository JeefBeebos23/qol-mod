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
public class RangedWeaponItemMixin {

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

        if (!QolConfig.getInstance().infinityBowEnabled) return;
        if (multishot) return;
        if (shooter.isInCreativeMode()) return;

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

        ItemStack intangible = projectileStack.copyWithCount(1);
        intangible.set(DataComponentTypes.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        cir.setReturnValue(intangible);
    }
}
