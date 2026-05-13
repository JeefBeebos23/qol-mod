package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin {

    @Redirect(
        method = "createResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z",
            ordinal = 1
        )
    )
    private boolean suppressTooExpensiveCreativeCheck(Player player) {
        if (QolConfig.getInstance().enchantLimitEnabled) return true;
        return player.hasInfiniteMaterials();
    }
}
