package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;


import java.util.Map;

public class CropReplantFeature {

    private static final Map<Block, Item> CROP_TO_SEED = Map.of(
        Blocks.WHEAT,            Items.WHEAT_SEEDS,
        Blocks.CARROTS,          Items.CARROT,
        Blocks.POTATOES,         Items.POTATO,
        Blocks.BEETROOTS,        Items.BEETROOT_SEEDS,
        Blocks.NETHER_WART,      Items.NETHER_WART,
        Blocks.TORCHFLOWER_CROP, Items.TORCHFLOWER_SEEDS
    );

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerLevel serverLevel)) return;
            if (!QolConfig.getInstance().cropReplantEnabled) return;
            if (!(player.getMainHandItem().getItem() instanceof HoeItem)) return;

            Block block = state.getBlock();
            Item seed = CROP_TO_SEED.get(block);
            if (seed == null) return;
            if (!isMature(block, state)) return;

            // Find seed in player's inventory
            Inventory inv = player.getInventory();
            int slot = -1;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).getItem() == seed) {
                    slot = i;
                    break;
                }
            }
            if (slot == -1) return;

            inv.getItem(slot).shrink(1);
            serverLevel.setBlock(pos, block.defaultBlockState(), 3);
        });
    }

    private static boolean isMature(Block block, BlockState state) {
        if (block instanceof CropBlock crop) return crop.isMaxAge(state);
        if (block instanceof NetherWartBlock)
            return state.getValue(NetherWartBlock.AGE) == 3;
        return false;
    }
}
