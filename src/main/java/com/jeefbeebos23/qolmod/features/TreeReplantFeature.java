package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;

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

    // Logs that can form 2x2 trees requiring 4 saplings
    private static final Set<Block> BIG_TREE_LOGS = Set.of(
        Blocks.SPRUCE_LOG, Blocks.DARK_OAK_LOG, Blocks.JUNGLE_LOG
    );

    // All blocks that can be the ground a tree grows on
    private static boolean isGround(BlockState state) {
        Block b = state.getBlock();
        return b == Blocks.DIRT
            || b == Blocks.GRASS_BLOCK
            || b == Blocks.PODZOL
            || b == Blocks.MYCELIUM
            || b == Blocks.COARSE_DIRT
            || b == Blocks.ROOTED_DIRT
            || b == Blocks.FARMLAND
            || b == Blocks.MOSS_BLOCK
            || b == Blocks.MUD
            || b == Blocks.GRAVEL   // acacia can spawn on gravel
            || b == Blocks.SAND;    // desert trees
    }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerLevel serverLevel)) return;
            if (!QolConfig.getInstance().treeReplantEnabled) return;

            Block sapling = LOG_TO_SAPLING.get(state.getBlock());
            if (sapling == null) return;

            // Scan downward through the now-cleared column to find ground.
            // VeinMiner destroys blocks without firing AFTER, so the column
            // is already air when this handler runs.
            BlockPos plantPos = pos;
            int limit = 64;
            while (limit-- > 0) {
                BlockState below = serverLevel.getBlockState(plantPos.below());
                if (isGround(below)) break;
                if (!below.isAir()) return;
                plantPos = plantPos.below();
            }
            if (limit < 0) return;
            if (!serverLevel.getBlockState(plantPos).isAir()) return;

            Item saplingItem = sapling.asItem();

            // For big tree types, try to plant a 2x2 if the footprint is clear
            if (BIG_TREE_LOGS.contains(state.getBlock())) {
                BlockPos origin = detect2x2Origin(serverLevel, plantPos);
                if (origin != null && consumeSaplings(player.getInventory(), saplingItem, 4)) {
                    for (int dx = 0; dx < 2; dx++) {
                        for (int dz = 0; dz < 2; dz++) {
                            serverLevel.setBlock(origin.offset(dx, 0, dz), sapling.defaultBlockState(), 3);
                        }
                    }
                    return;
                }
            }

            // Fallback: single sapling
            if (consumeSaplings(player.getInventory(), saplingItem, 1)) {
                serverLevel.setBlock(plantPos, sapling.defaultBlockState(), 3);
            }
        });
    }

    // Returns the NW corner of a 2x2 footprint that includes plantPos, or null if no clear 2x2 exists.
    private static BlockPos detect2x2Origin(ServerLevel level, BlockPos plantPos) {
        // plantPos may be any of the 4 corners — try each possible NW origin
        int[][] offsets = { {0, 0}, {-1, 0}, {0, -1}, {-1, -1} };
        for (int[] off : offsets) {
            BlockPos origin = plantPos.offset(off[0], 0, off[1]);
            if (is2x2Clear(level, origin, plantPos.getY())) return origin;
        }
        return null;
    }

    private static boolean is2x2Clear(ServerLevel level, BlockPos origin, int y) {
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                BlockPos cell = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                if (!level.getBlockState(cell).isAir()) return false;
                if (!isGround(level.getBlockState(cell.below()))) return false;
            }
        }
        return true;
    }

    private static boolean consumeSaplings(Inventory inv, Item saplingItem, int needed) {
        int found = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).getItem() == saplingItem) found += inv.getItem(i).getCount();
        }
        if (found < needed) return false;
        int remaining = needed;
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == saplingItem) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
        return true;
    }
}
