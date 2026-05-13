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

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!QolConfig.getInstance().furnaceXpEnabled) return;

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("qolmod.furnace.collect"),
                btn -> {
                    if (!QolConfig.getInstance().furnaceXpEnabled) return;
                    ClientPlayNetworking.send(new FurnaceXpCollectPayload());
                }
        ).dimensions(x + 97, y + 54, 72, 14).build());

        ClientPlayNetworking.send(new FurnaceXpRequestPayload());
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void onDrawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!QolConfig.getInstance().furnaceXpEnabled) return;

        int xp = FurnaceXpFeature.clientStoredXp;
        String label = Text.translatable("qolmod.furnace.xp", xp).getString();
        context.drawText(this.textRenderer, label, x + 97, y + 46, 0x404040, false);
    }
}
