package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.FurnaceXpCollectPayload;
import com.jeefbeebos23.qolmod.features.FurnaceXpFeature;
import com.jeefbeebos23.qolmod.features.FurnaceXpRequestPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a stored-XP display and "Collect XP" button to all furnace screens
 * (Furnace, Blast Furnace, Smoker) when {@code furnaceXpEnabled} is true.
 *
 * BlockPos approach: The client does NOT have access to the furnace block pos via
 * the screen handler (the client-side handler holds a SimpleInventory, not the
 * block entity). Instead, both C2S packets carry no position — the server looks up
 * the furnace from the player's currently open screen handler, which IS the real
 * block entity on the server side.
 */
@Environment(EnvType.CLIENT)
@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin<T extends AbstractFurnaceScreenHandler>
        extends HandledScreen<T> {

    // Dummy constructor required by Mixin compiler (never called at runtime)
    protected AbstractFurnaceScreenMixin(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    // x and y are inherited as protected fields from HandledScreen — no @Shadow needed.

    // -------------------------------------------------------------------------
    // init: add "Collect XP" button and request current XP from server
    // -------------------------------------------------------------------------

    @Inject(method = "init", at = @At("TAIL"))
    private void qol$onInit(CallbackInfo ci) {
        if (!QolConfig.getInstance().furnaceXpEnabled) return;

        // "Collect XP" button — positioned at the right side of the furnace background,
        // just below the burn-progress arrow, above the player inventory divider.
        // The furnace background is 176 wide; output slot is at +116,+35.
        // We place the button at (x + 97, y + 54), size 72x14.
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("qolmod.furnace.collect"),
                btn -> ClientPlayNetworking.send(new FurnaceXpCollectPayload())
        ).dimensions(x + 97, y + 54, 72, 14).build());

        // Ask the server for the current stored XP
        ClientPlayNetworking.send(new FurnaceXpRequestPayload());
    }

    // -------------------------------------------------------------------------
    // drawBackground: draw "Stored XP: N" text inside the furnace GUI
    // -------------------------------------------------------------------------

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void qol$onDrawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!QolConfig.getInstance().furnaceXpEnabled) return;

        int xp = FurnaceXpFeature.clientStoredXp;
        String label = Text.translatable("qolmod.furnace.xp", xp).getString();
        // Draw above the "Collect XP" button, inside the background panel
        context.drawText(this.textRenderer, label, x + 97, y + 46, 0x404040, false);
    }
}
