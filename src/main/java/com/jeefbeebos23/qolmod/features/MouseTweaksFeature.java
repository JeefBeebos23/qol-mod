package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MouseTweaksFeature {

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(MouseTweaksMoveOnePayload.ID, MouseTweaksMoveOnePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MouseTweaksMoveOnePayload.ID, (payload, ctx) ->
            ctx.server().execute(() -> {
                if (!QolConfig.getInstance().mouseTweaksEnabled) return;
                ServerPlayer player = ctx.player();
                AbstractContainerMenu menu = player.containerMenu;
                for (int slotIndex : payload.slotIndices()) {
                    if (slotIndex < 0 || slotIndex >= menu.slots.size()) continue;
                    Slot slot = menu.getSlot(slotIndex);
                    ItemStack current = slot.getItem();
                    if (current.isEmpty()) continue;

                    int originalCount = current.getCount();
                    ItemStack backup = current.copy();

                    // Temporarily reduce slot to 1 item so quickMoveStack moves exactly 1.
                    slot.set(current.copyWithCount(1));
                    menu.quickMoveStack(player, slotIndex);

                    if (slot.getItem().isEmpty()) {
                        // The 1 item was moved; restore the remaining originalCount-1
                        if (originalCount > 1) {
                            slot.set(backup.copyWithCount(originalCount - 1));
                            slot.setChanged();
                        }
                    } else {
                        // Move failed (no space); restore original count
                        slot.set(backup);
                        slot.setChanged();
                    }
                }
                menu.broadcastChanges();
            })
        );
    }
}
