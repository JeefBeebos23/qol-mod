package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class TreeReplantFeature {

    private static final Map<Block, Block> LOG_TO_SAPLING = Map.ofEntries(
        Map.entry(Blocks.OAK_LOG,      Blocks.OAK_SAPLING),
        Map.entry(Blocks.SPRUCE_LOG,   Blocks.SPRUCE_SAPLING),
        Map.entry(Blocks.BIRCH_LOG,    Blocks.BIRCH_SAPLING),
        Map.entry(Blocks.JUNGLE_LOG,   Blocks.JUNGLE_SAPLING),
        Map.entry(Blocks.ACACIA_LOG,   Blocks.ACACIA_SAPLING),
        Map.entry(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_SAPLING),
        Map.entry(Blocks.CHERRY_LOG,   Blocks.CHERRY_SAPLING),
        Map.entry(Blocks.MANGROVE_LOG, Blocks.MANGROVE_PROPAGULE),
        Map.entry(Blocks.PALE_OAK_LOG, Blocks.PALE_OAK_SAPLING)
    );

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerLevel serverLevel)) return;
            if (!QolConfig.getInstance().treeReplantEnabled) return;

            Block sapling = LOG_TO_SAPLING.get(state.getBlock());
            if (sapling == null) return;

            // Only replant at the base of a tree (ground must be below)
            BlockState below = serverLevel.getBlockState(pos.below());
            if (!below.is(BlockTags.DIRT) && !below.is(Blocks.FARMLAND)) return;

            // Find the sapling item in the player's inventory
            var saplingItem = sapling.asItem();
            Inventory inv = player.getInventory();
            int slot = -1;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).getItem() == saplingItem) {
                    slot = i;
                    break;
                }
            }
            if (slot == -1) return;

            inv.getItem(slot).shrink(1);
            serverLevel.setBlock(pos, sapling.defaultBlockState(), 3);
        });
    }
}
