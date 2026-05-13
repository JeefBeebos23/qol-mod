package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class VeinMinerFeature {

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld)) return;
            if (!QolConfig.getInstance().veinMinerEnabled) return;
            if (!activePlayers.contains(player.getUuid())) return;

            String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
            if (!QolConfig.getInstance().veinMinerBlocks.contains(blockId)) return;

            Set<BlockPos> vein = findVein(pos, QolConfig.getInstance().veinMinerMaxBlocks,
                n -> serverWorld.getBlockState(n).getBlock() == state.getBlock());
            vein.remove(pos);

            for (BlockPos veinPos : vein) {
                serverWorld.breakBlock(veinPos, true, player);
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
                        BlockPos n = p.add(dx, dy, dz);
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
