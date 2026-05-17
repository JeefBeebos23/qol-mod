package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class RestockFeature {

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(RestockPayload.ID, RestockPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RestockPayload.ID, (payload, ctx) ->
            ctx.server().execute(() -> {
                if (!QolConfig.getInstance().restockEnabled) return;
                restock(ctx.player());
            })
        );
    }

    private static void restock(ServerPlayer player) {
        int radius = QolConfig.getInstance().autoStackRadius;
        Inventory inv = player.getInventory();
        BlockPos playerPos = player.blockPosition();
        BlockPos.betweenClosed(
            playerPos.offset(-radius, -radius, -radius),
            playerPos.offset(radius, radius, radius)
        ).forEach(pos -> {
            if (player.level().getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                restockFromChest(inv, chest);
            }
        });
    }

    private static void restockFromChest(Inventory playerInv, Container chest) {
        for (int pi = 0; pi < 36; pi++) {
            ItemStack playerStack = playerInv.getItem(pi);
            if (playerStack.isEmpty()) continue;
            if (playerStack.getCount() >= playerStack.getMaxStackSize()) continue;

            for (int ci = 0; ci < chest.getContainerSize(); ci++) {
                ItemStack chestStack = chest.getItem(ci);
                if (chestStack.isEmpty()) continue;
                if (!ItemStack.isSameItem(chestStack, playerStack)) continue;

                int space = playerStack.getMaxStackSize() - playerStack.getCount();
                int transfer = Math.min(space, chestStack.getCount());
                playerStack.grow(transfer);
                chestStack.shrink(transfer);
                chest.setChanged();
                if (playerStack.getCount() >= playerStack.getMaxStackSize()) break;
            }
        }
    }
}
