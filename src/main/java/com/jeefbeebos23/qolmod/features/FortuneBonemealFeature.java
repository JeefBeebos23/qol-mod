package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FortuneBonemealFeature {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(FortuneBonemealFeature::onBlockBreak);
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!QolConfig.getInstance().fortuneBonemealEnabled) return InteractionResult.PASS;

            ItemStack heldItem = player.getItemInHand(hand);
            if (!heldItem.is(Items.BONE_MEAL)) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ServerLevel level = (ServerLevel) world;

            if (state.is(Blocks.SUGAR_CANE))
                return tryGrowColumn(level, pos, Blocks.SUGAR_CANE, player, heldItem);
            if (state.is(Blocks.CACTUS))
                return tryGrowColumn(level, pos, Blocks.CACTUS, player, heldItem);
            if (state.is(Blocks.NETHER_WART))
                return tryAdvanceAge(level, pos, state, NetherWartBlock.AGE, 3, player, heldItem);
            if (state.is(Blocks.SWEET_BERRY_BUSH))
                return tryAdvanceAge(level, pos, state, SweetBerryBushBlock.AGE, 3, player, heldItem);

            return InteractionResult.PASS;
        });
    }

    private static void onBlockBreak(net.minecraft.world.level.Level world, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        if (!QolConfig.getInstance().fortuneBonemealEnabled) return;
        if (player.getAbilities().instabuild) return;

        int fortune = getFortune(player.getMainHandItem());
        if (fortune == 0) return;

        ItemStack bonus = fortuneBonus(state, player, fortune);
        if (!bonus.isEmpty()) Block.popResource(world, pos, bonus);
    }

    // Returns extra items to drop based on fortune level, or EMPTY if this block type isn't handled.
    private static ItemStack fortuneBonus(BlockState state, Player player, int fortune) {
        int extra = player.getRandom().nextInt(fortune + 1);
        if (extra == 0) return ItemStack.EMPTY;

        Block block = state.getBlock();
        if (block == Blocks.PUMPKIN)     return new ItemStack(Items.PUMPKIN, extra);
        if (block == Blocks.SUGAR_CANE)  return new ItemStack(Items.SUGAR_CANE, extra);
        if (block == Blocks.BAMBOO)      return new ItemStack(Items.BAMBOO, extra);
        if (block == Blocks.CACTUS)      return new ItemStack(Items.CACTUS, extra);
        if (block instanceof NetherWartBlock && state.getValue(NetherWartBlock.AGE) == 3)
            return new ItemStack(Items.NETHER_WART, extra);
        if (block instanceof CocoaBlock && state.getValue(CocoaBlock.AGE) == 2)
            return new ItemStack(Items.COCOA_BEANS, extra);

        return ItemStack.EMPTY;
    }

    // Finds the top of a column plant and grows it by one block if below max height (3).
    private static InteractionResult tryGrowColumn(ServerLevel level, BlockPos pos, Block plantBlock, Player player, ItemStack boneMeal) {
        BlockPos top = pos;
        while (level.getBlockState(top.above()).is(plantBlock)) top = top.above();

        BlockPos base = pos;
        while (level.getBlockState(base.below()).is(plantBlock)) base = base.below();

        int height = top.getY() - base.getY() + 1;
        if (height >= 3 || !level.getBlockState(top.above()).isAir()) return InteractionResult.PASS;

        level.setBlock(top.above(), plantBlock.defaultBlockState(), 3);
        applyBonemeal(player, boneMeal, level, pos);
        return InteractionResult.SUCCESS;
    }

    // Advances a block's integer age property by one stage if not already at max.
    private static InteractionResult tryAdvanceAge(ServerLevel level, BlockPos pos, BlockState state, IntegerProperty ageProperty, int maxAge, Player player, ItemStack boneMeal) {
        int age = state.getValue(ageProperty);
        if (age >= maxAge) return InteractionResult.PASS;
        level.setBlock(pos, state.setValue(ageProperty, age + 1), 3);
        applyBonemeal(player, boneMeal, level, pos);
        return InteractionResult.SUCCESS;
    }

    private static void applyBonemeal(Player player, ItemStack boneMeal, ServerLevel level, BlockPos pos) {
        level.levelEvent(2005, pos, 0);
        if (!player.getAbilities().instabuild) boneMeal.shrink(1);
    }

    private static int getFortune(ItemStack stack) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (holder.is(Enchantments.FORTUNE)) return enchantments.getLevel(holder);
        }
        return 0;
    }
}
