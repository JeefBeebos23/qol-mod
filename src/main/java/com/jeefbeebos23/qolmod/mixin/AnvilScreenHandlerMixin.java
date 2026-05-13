package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Removes the "Too Expensive!" cap on the anvil.
 *
 * Vanilla AnvilScreenHandler.updateResult() blocks the operation when
 * levelCost >= 40 by checking player.getAbilities().creativeMode — if not
 * in creative mode the output is cleared.  We redirect that abilities read
 * and, when the feature is enabled, pretend the player is always in creative
 * mode for this specific check so the clear never happens.
 *
 * In Yarn 1.21.4, updateResult() contains two getAbilities() calls:
 *   ordinal 0: inside the enchantment-acceptance loop
 *              "this.player.getAbilities().creativeMode || itemStack.isOf(ENCHANTED_BOOK)"
 *   ordinal 1: the "Too Expensive" guard
 *              "if (levelCost >= 40 && !this.player.getAbilities().creativeMode)"
 *
 * We only redirect ordinal 1.
 */
@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {

    /**
     * Redirect the second getAbilities() call inside updateResult() (ordinal = 1).
     * When enchantLimitEnabled is true, return a fake PlayerAbilities with
     * creativeMode = true so the "Too Expensive" branch never clears the output.
     */
    @Redirect(
        method = "updateResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getAbilities()Lnet/minecraft/entity/player/PlayerAbilities;",
            ordinal = 1
        )
    )
    private PlayerAbilities qolmod$suppressTooExpensiveCreativeCheck(PlayerEntity player) {
        if (QolConfig.getInstance().enchantLimitEnabled) {
            // Return a fake PlayerAbilities with creativeMode = true so the
            // "!creativeMode" guard evaluates to false and the output is kept.
            PlayerAbilities fakeAbilities = new PlayerAbilities();
            fakeAbilities.creativeMode = true;
            return fakeAbilities;
        }
        return player.getAbilities();
    }
}
