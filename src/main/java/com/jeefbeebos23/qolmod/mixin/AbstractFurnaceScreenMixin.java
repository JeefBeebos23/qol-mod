package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.FurnaceXpCollectPayload;
import com.jeefbeebos23.qolmod.features.FurnaceXpFeature;
import com.jeefbeebos23.qolmod.features.FurnaceXpRequestPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin<T extends AbstractFurnaceMenu>
        extends AbstractContainerScreen<T> {

    protected AbstractFurnaceScreenMixin(T handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!QolConfig.getInstance().furnaceXpEnabled) return;

        addRenderableWidget(Button.builder(
                Component.translatable("qolmod.furnace.collect"),
                btn -> {
                    if (!QolConfig.getInstance().furnaceXpEnabled) return;
                    ClientPlayNetworking.send(new FurnaceXpCollectPayload());
                }
        ).pos(leftPos + 97, topPos + 54).size(72, 14).build());

        ClientPlayNetworking.send(new FurnaceXpRequestPayload());
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void onExtractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!QolConfig.getInstance().furnaceXpEnabled) return;

        int xp = FurnaceXpFeature.clientStoredXp;
        String label = Component.translatable("qolmod.furnace.xp", xp).getString();
        context.text(this.font, label, leftPos + 97, topPos + 46, 0x404040, false);
    }
}
