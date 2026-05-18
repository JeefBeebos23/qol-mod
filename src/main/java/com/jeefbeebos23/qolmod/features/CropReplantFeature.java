package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CropReplantFeature {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!QolConfig.getInstance().cropReplantEnabled) return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player.getMainHandItem().getItem() instanceof HoeItem)) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (!isMature(block, state)) return InteractionResult.PASS;

            // Drop loot (fortune-aware via the player's held tool)
            Block.dropResources(state, (ServerLevel) world, pos, null, player, player.getMainHandItem());
            // Reset crop to age 0 — stays planted, no seed needed from inventory
            world.setBlock(pos, block.defaultBlockState(), 3);

            return InteractionResult.SUCCESS;
        });
    }

    private static boolean isMature(Block block, BlockState state) {
        if (block instanceof CropBlock crop) return crop.isMaxAge(state);
        if (block instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) == 3;
        return false;
    }
}
