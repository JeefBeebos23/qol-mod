package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class VeinMinerFeature {

    private static final TagKey<Block> ORES_TAG =
        TagKey.create(Registries.BLOCK, Identifier.parse("c:ores"));

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerLevel serverLevel)) return;
            if (!QolConfig.getInstance().veinMinerEnabled) return;
            if (!activePlayers.contains(player.getUUID())) return;

            String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            boolean inList = QolConfig.getInstance().veinMinerBlocks.contains(blockId);
            boolean isOre = state.is(ORES_TAG) || blockId.endsWith("_ore");
            if (!inList && !isOre) return;

            Set<BlockPos> vein = findVein(pos, QolConfig.getInstance().veinMinerMaxBlocks,
                n -> serverLevel.getBlockState(n).getBlock() == state.getBlock());
            vein.remove(pos);

            for (BlockPos veinPos : vein) {
                BlockState veinState = serverLevel.getBlockState(veinPos);
                BlockEntity veinBE = serverLevel.getBlockEntity(veinPos);
                serverLevel.destroyBlock(veinPos, false, player);
                Block.dropResources(veinState, serverLevel, pos, veinBE, player, player.getMainHandItem());
            }
        });
    }

    public static Set<BlockPos> findVein(BlockPos origin, int maxBlocks, Predicate<BlockPos> isSameType) {
        Set<BlockPos> result = new LinkedHashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(origin);
        result.add(origin);

        while (!queue.isEmpty() && result.size() < maxBlocks) {
            BlockPos p = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos n = p.offset(dx, dy, dz);
                        if (!result.contains(n) && isSameType.test(n)) {
                            result.add(n);
                            queue.add(n);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void setActive(UUID playerId, boolean active) {
        if (active) activePlayers.add(playerId);
        else activePlayers.remove(playerId);
    }
}
