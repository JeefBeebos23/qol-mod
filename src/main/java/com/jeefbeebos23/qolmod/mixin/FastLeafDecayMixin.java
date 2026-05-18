package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public class FastLeafDecayMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void fastDecay(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!QolConfig.getInstance().fastLeafDecayEnabled) return;
        BlockState current = level.getBlockState(pos);
        if (!current.is(BlockTags.LEAVES)) return;
        if (current.getValue(LeavesBlock.PERSISTENT) || current.getValue(LeavesBlock.DISTANCE) < 7) return;

        Block.dropResources(current, level, pos);
        level.removeBlock(pos, false);

        for (Direction dir : Direction.values()) {
            BlockPos adj = pos.relative(dir);
            BlockState adjState = level.getBlockState(adj);
            if (adjState.is(BlockTags.LEAVES) && !adjState.getValue(LeavesBlock.PERSISTENT)) {
                level.scheduleTick(adj, adjState.getBlock(), 1);
            }
        }
    }
}
