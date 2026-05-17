package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.QuickStackPayload;
import com.jeefbeebos23.qolmod.features.RestockPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    protected InventoryScreenMixin(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addQuickStackButton(CallbackInfo ci) {
        if (!QolConfig.getInstance().autoStackEnabled) return;
        addRenderableWidget(Button.builder(
            Component.translatable("qolmod.quickstack"),
            btn -> ClientPlayNetworking.send(new QuickStackPayload()))
            .pos(this.leftPos + this.imageWidth + 4, this.topPos + 60)
            .size(70, 20)
            .build()
        );
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addRestockButton(CallbackInfo ci) {
        if (!QolConfig.getInstance().restockEnabled) return;
        addRenderableWidget(Button.builder(
            Component.translatable("qolmod.restock"),
            btn -> ClientPlayNetworking.send(new RestockPayload()))
            .pos(this.leftPos + this.imageWidth + 4, this.topPos + 82)
            .size(70, 20)
            .build()
        );
    }
}
