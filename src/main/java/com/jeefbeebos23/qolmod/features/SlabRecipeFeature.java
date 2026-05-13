package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlabRecipeFeature {

    private static final Map<Item, Item> SLAB_TO_BLOCK = Map.ofEntries(
        Map.entry(Items.OAK_SLAB, Items.OAK_PLANKS),
        Map.entry(Items.SPRUCE_SLAB, Items.SPRUCE_PLANKS),
        Map.entry(Items.BIRCH_SLAB, Items.BIRCH_PLANKS),
        Map.entry(Items.JUNGLE_SLAB, Items.JUNGLE_PLANKS),
        Map.entry(Items.ACACIA_SLAB, Items.ACACIA_PLANKS),
        Map.entry(Items.DARK_OAK_SLAB, Items.DARK_OAK_PLANKS),
        Map.entry(Items.MANGROVE_SLAB, Items.MANGROVE_PLANKS),
        Map.entry(Items.CHERRY_SLAB, Items.CHERRY_PLANKS),
        Map.entry(Items.BAMBOO_SLAB, Items.BAMBOO_PLANKS),
        Map.entry(Items.CRIMSON_SLAB, Items.CRIMSON_PLANKS),
        Map.entry(Items.WARPED_SLAB, Items.WARPED_PLANKS),
        Map.entry(Items.STONE_SLAB, Items.STONE),
        Map.entry(Items.SMOOTH_STONE_SLAB, Items.SMOOTH_STONE),
        Map.entry(Items.COBBLESTONE_SLAB, Items.COBBLESTONE),
        Map.entry(Items.MOSSY_COBBLESTONE_SLAB, Items.MOSSY_COBBLESTONE),
        Map.entry(Items.STONE_BRICK_SLAB, Items.STONE_BRICKS),
        Map.entry(Items.MOSSY_STONE_BRICK_SLAB, Items.MOSSY_STONE_BRICKS),
        Map.entry(Items.SANDSTONE_SLAB, Items.SANDSTONE),
        Map.entry(Items.SMOOTH_SANDSTONE_SLAB, Items.SMOOTH_SANDSTONE),
        Map.entry(Items.RED_SANDSTONE_SLAB, Items.RED_SANDSTONE),
        Map.entry(Items.CUT_RED_SANDSTONE_SLAB, Items.CUT_RED_SANDSTONE),
        Map.entry(Items.SMOOTH_RED_SANDSTONE_SLAB, Items.SMOOTH_RED_SANDSTONE),
        Map.entry(Items.BRICK_SLAB, Items.BRICKS),
        Map.entry(Items.NETHER_BRICK_SLAB, Items.NETHER_BRICKS),
        Map.entry(Items.RED_NETHER_BRICK_SLAB, Items.RED_NETHER_BRICKS),
        Map.entry(Items.QUARTZ_SLAB, Items.QUARTZ_BLOCK),
        Map.entry(Items.SMOOTH_QUARTZ_SLAB, Items.SMOOTH_QUARTZ),
        Map.entry(Items.PURPUR_SLAB, Items.PURPUR_BLOCK),
        Map.entry(Items.PRISMARINE_SLAB, Items.PRISMARINE),
        Map.entry(Items.PRISMARINE_BRICK_SLAB, Items.PRISMARINE_BRICKS),
        Map.entry(Items.DARK_PRISMARINE_SLAB, Items.DARK_PRISMARINE),
        Map.entry(Items.END_STONE_BRICK_SLAB, Items.END_STONE_BRICKS),
        Map.entry(Items.BLACKSTONE_SLAB, Items.BLACKSTONE),
        Map.entry(Items.POLISHED_BLACKSTONE_SLAB, Items.POLISHED_BLACKSTONE),
        Map.entry(Items.POLISHED_BLACKSTONE_BRICK_SLAB, Items.POLISHED_BLACKSTONE_BRICKS),
        Map.entry(Items.COBBLED_DEEPSLATE_SLAB, Items.COBBLED_DEEPSLATE),
        Map.entry(Items.POLISHED_DEEPSLATE_SLAB, Items.POLISHED_DEEPSLATE),
        Map.entry(Items.DEEPSLATE_BRICK_SLAB, Items.DEEPSLATE_BRICKS),
        Map.entry(Items.DEEPSLATE_TILE_SLAB, Items.DEEPSLATE_TILES),
        Map.entry(Items.TUFF_SLAB, Items.TUFF),
        Map.entry(Items.POLISHED_TUFF_SLAB, Items.POLISHED_TUFF),
        Map.entry(Items.TUFF_BRICK_SLAB, Items.TUFF_BRICKS),
        Map.entry(Items.MUD_BRICK_SLAB, Items.MUD_BRICKS),
        Map.entry(Items.CUT_COPPER_SLAB, Items.CUT_COPPER),
        Map.entry(Items.EXPOSED_CUT_COPPER_SLAB, Items.EXPOSED_CUT_COPPER),
        Map.entry(Items.WEATHERED_CUT_COPPER_SLAB, Items.WEATHERED_CUT_COPPER),
        Map.entry(Items.OXIDIZED_CUT_COPPER_SLAB, Items.OXIDIZED_CUT_COPPER),
        Map.entry(Items.WAXED_CUT_COPPER_SLAB, Items.WAXED_CUT_COPPER),
        Map.entry(Items.WAXED_EXPOSED_CUT_COPPER_SLAB, Items.WAXED_EXPOSED_CUT_COPPER),
        Map.entry(Items.WAXED_WEATHERED_CUT_COPPER_SLAB, Items.WAXED_WEATHERED_CUT_COPPER),
        Map.entry(Items.WAXED_OXIDIZED_CUT_COPPER_SLAB, Items.WAXED_OXIDIZED_CUT_COPPER)
    );

    public static List<RecipeHolder<?>> buildRecipes() {
        List<RecipeHolder<?>> entries = new ArrayList<>();
        SLAB_TO_BLOCK.forEach((slab, block) -> {
            Identifier id = Identifier.fromNamespaceAndPath(QolMod.MOD_ID,
                BuiltInRegistries.ITEM.getKey(slab).getPath() + "_to_block");
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
            List<Ingredient> inputs = List.of(
                Ingredient.of(slab),
                Ingredient.of(slab)
            );
            ShapelessRecipe recipe = new ShapelessRecipe(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.BUILDING, ""),
                new ItemStackTemplate(block),
                inputs
            );
            entries.add(new RecipeHolder<>(key, recipe));
        });
        return entries;
    }
}
