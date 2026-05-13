package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Removes the "Too Expensive!" cap on the anvil.
 *
 * Vanilla AnvilMenu.createResult() blocks the operation when
 * levelCost >= 40 unless the player is in creative mode. We redirect
 * that abilities read and pretend the player is always in creative mode
 * for this specific check so the clear never happens.
 *
 * In 26.1.2, createResult() contains two getAbilities() calls:
 *   ordinal 0: inside the enchantment-acceptance loop
 *   ordinal 1: the "Too Expensive" guard
 *
 * We only redirect ordinal 1.
 */
@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin {

    @Redirect(
        method = "createResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAbilities()Lnet/minecraft/world/entity/player/Abilities;",
            ordinal = 1
        )
    )
    private Abilities suppressTooExpensiveCreativeCheck(Player player) {
        if (QolConfig.getInstance().enchantLimitEnabled) {
            Abilities fakeAbilities = new Abilities();
            fakeAbilities.instabuild = true;
            return fakeAbilities;
        }
        return player.getAbilities();
    }
}
