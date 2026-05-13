package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.HashSet;
import java.util.Set;

public class AutoStackFeature {

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(QuickStackPayload.ID, QuickStackPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(QuickStackPayload.ID, (payload, ctx) ->
            ctx.server().execute(() -> {
                if (!QolConfig.getInstance().autoStackEnabled) return;
                quickStack(ctx.player());
            })
        );
    }

    private static void quickStack(ServerPlayer player) {
        int radius = QolConfig.getInstance().autoStackRadius;
        Inventory inv = player.getInventory();
        BlockPos playerPos = player.blockPosition();
        BlockPos.betweenClosed(
            playerPos.offset(-radius, -radius, -radius),
            playerPos.offset(radius, radius, radius)
        ).forEach(pos -> {
            if (player.level().getBlockEntity(pos) instanceof ChestBlockEntity chest) {
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
