package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.HashSet;
import java.util.Set;

public class AutoStackFeature {

    public static void registerClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen)) return;
            if (!QolConfig.getInstance().autoStackEnabled) return;

            int x = screen.width / 2 + 60;
            int y = screen.height / 2 + 40;

            Screens.getWidgets(screen).add(
                Button.builder(Component.translatable("qolmod.quickstack"), btn -> {
                    if (client.player != null) quickStack(client.player, client.level);
                })
                .pos(x, y)
                .size(60, 12)
                .build()
            );
        });
    }

    private static void quickStack(Player player, Level world) {
        if (world == null) return;
        int radius = QolConfig.getInstance().autoStackRadius;
        Inventory inv = player.getInventory();

        BlockPos playerPos = player.blockPosition();
        BlockPos.betweenClosed(
            playerPos.offset(-radius, -radius, -radius),
            playerPos.offset(radius, radius, radius)
        ).forEach(pos -> {
            if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                stackToChest(inv, chest);
            }
        });
    }

    private static void stackToChest(Inventory playerInv, Container chest) {
        Set<Item> chestItems = new HashSet<>();
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty()) chestItems.add(stack.getItem());
        }

        for (int pi = 0; pi < 36; pi++) {
            ItemStack playerStack = playerInv.getItem(pi);
            if (playerStack.isEmpty()) continue;
            if (!chestItems.contains(playerStack.getItem())) continue;

            for (int ci = 0; ci < chest.getContainerSize(); ci++) {
                ItemStack chestStack = chest.getItem(ci);
                if (chestStack.isEmpty()) continue;
                if (!ItemStack.isSameItem(chestStack, playerStack)) continue;
                int space = chestStack.getMaxStackSize() - chestStack.getCount();
                if (space <= 0) continue;
                int transfer = Math.min(space, playerStack.getCount());
                chestStack.grow(transfer);
                playerStack.shrink(transfer);
                chest.setChanged();
                if (playerStack.isEmpty()) break;
            }
        }
    }
}
