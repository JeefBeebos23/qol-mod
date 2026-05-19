package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.HotbarLayout;
import com.jeefbeebos23.qolmod.features.InventorySortPayload;
import com.jeefbeebos23.qolmod.features.MagicMirrorButton;
import com.jeefbeebos23.qolmod.features.MagicMirrorPayload;
import com.jeefbeebos23.qolmod.features.QuickStackPayload;
import com.jeefbeebos23.qolmod.features.RestockPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

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
    private void addInventoryButtons(CallbackInfo ci) {
        int bx = this.leftPos + this.imageWidth + 4;

        if (QolConfig.getInstance().inventoryRestockEnabled) {
            addRenderableWidget(Button.builder(
                Component.translatable("qolmod.restock"),
                btn -> ClientPlayNetworking.send(new RestockPayload()))
                .pos(bx, this.topPos + 84)
                .size(70, 20)
                .build()
            );
        }

        if (QolConfig.getInstance().magicMirrorEnabled) {
            addRenderableWidget(new MagicMirrorButton(
                this.leftPos + 81, this.height / 2 - 22,
                btn -> ClientPlayNetworking.send(new MagicMirrorPayload())
            ));
        }

        if (QolConfig.getInstance().inventorySortEnabled) {
            addRenderableWidget(Button.builder(
                Component.translatable("qolmod.sort"),
                btn -> ClientPlayNetworking.send(new InventorySortPayload(HotbarLayout.load())))
                .pos(bx, this.topPos + 108)
                .size(70, 20)
                .build()
            );

            addRenderableWidget(Button.builder(
                Component.translatable("qolmod.savelayout"),
                btn -> {
                    var mc = Minecraft.getInstance();
                    if (mc.player == null) return;
                    List<String> layout = new ArrayList<>(10);
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.player.getInventory().getItem(i);
                        var regId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                        layout.add(stack.isEmpty() || regId == null ? null : regId.toString());
                    }
                    // Index 9 = offhand (inventory slot 40)
                    ItemStack offhand = mc.player.getInventory().getItem(40);
                    var offhandId = BuiltInRegistries.ITEM.getKey(offhand.getItem());
                    layout.add(offhand.isEmpty() || offhandId == null ? null : offhandId.toString());
                    HotbarLayout.save(layout);
                })
                .pos(bx, this.topPos + 132)
                .size(70, 20)
                .build()
            );
        }
    }
}
