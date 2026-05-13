package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class AutoStackFeature {

    public static void registerClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen)) return;
            if (!QolConfig.getInstance().autoStackEnabled) return;

            int x = screen.width / 2 + 60;
            int y = screen.height / 2 + 40;

            Screens.getButtons(screen).add(
                ButtonWidget.builder(Text.translatable("qolmod.quickstack"), btn -> {
                    if (client.player != null) quickStack(client.player, client.world);
                })
                .position(x, y)
                .size(60, 12)
                .build()
            );
        });
    }

    private static void quickStack(PlayerEntity player, World world) {
        if (world == null) return;
        int radius = QolConfig.getInstance().autoStackRadius;
        PlayerInventory inv = player.getInventory();

        BlockPos playerPos = player.getBlockPos();
        BlockPos.iterate(
            playerPos.add(-radius, -radius, -radius),
            playerPos.add(radius, radius, radius)
        ).forEach(pos -> {
            if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                stackToChest(inv, chest);
            }
        });
    }

    private static void stackToChest(PlayerInventory playerInv, Inventory chest) {
        Set<Item> chestItems = new HashSet<>();
        for (int i = 0; i < chest.size(); i++) {
            ItemStack stack = chest.getStack(i);
            if (!stack.isEmpty()) chestItems.add(stack.getItem());
        }

        for (int pi = 0; pi < playerInv.main.size(); pi++) {
            ItemStack playerStack = playerInv.main.get(pi);
            if (playerStack.isEmpty()) continue;
            if (!chestItems.contains(playerStack.getItem())) continue;

            for (int ci = 0; ci < chest.size(); ci++) {
                ItemStack chestStack = chest.getStack(ci);
                if (chestStack.isEmpty()) continue;
                if (!ItemStack.areItemsEqual(chestStack, playerStack)) continue;
                int space = chestStack.getMaxCount() - chestStack.getCount();
                if (space <= 0) continue;
                int transfer = Math.min(space, playerStack.getCount());
                chestStack.increment(transfer);
                playerStack.decrement(transfer);
                chest.markDirty();
                if (playerStack.isEmpty()) break;
            }
        }
    }
}
