package com.jeefbeebos23.qolmod.features;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InventorySorter {

    // Wood types in display order; each gets its own category bucket so they don't mix.
    private static final List<String> WOOD_TYPES = List.of(
        "oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
        "mangrove", "cherry", "bamboo", "pale_oak", "crimson", "warped"
    );

    // ── public sort entry points ──────────────────────────────────────────────

    public static void sort(ServerPlayer player, List<String> hotbarLayout) {
        Inventory inv = player.getInventory();

        List<ItemStack> pool = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) pool.add(stack);
            inv.setItem(i, ItemStack.EMPTY);
        }

        for (int slot = 0; slot < 9; slot++) {
            String savedId = slot < hotbarLayout.size() ? hotbarLayout.get(slot) : null;
            if (savedId == null) continue;
            ItemStack best = findBestForLayout(pool, savedId);
            if (best != null) {
                inv.setItem(slot, best);
                pool.remove(best);
            }
        }

        for (EquipmentSlot armorSlot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack best = findBestArmor(pool, armorSlot);
            if (best != null) {
                inv.setItem(armorInventorySlot(armorSlot), best);
                pool.remove(best);
            }
        }

        sortPool(pool);

        int idx = 0;
        for (int slot = 9; slot < 36 && idx < pool.size(); slot++) {
            if (inv.getItem(slot).isEmpty()) inv.setItem(slot, pool.get(idx++));
        }
        for (int slot = 0; slot < 9 && idx < pool.size(); slot++) {
            if (inv.getItem(slot).isEmpty()) inv.setItem(slot, pool.get(idx++));
        }
    }

    public static void sortChest(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (!(menu instanceof ChestMenu chestMenu)) return;

        Container container = chestMenu.getContainer();
        int size = container.getContainerSize();

        List<ItemStack> pool = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) pool.add(stack);
            container.setItem(i, ItemStack.EMPTY);
        }

        sortPool(pool);

        for (int i = 0; i < pool.size(); i++) container.setItem(i, pool.get(i));
        container.setChanged();
        chestMenu.broadcastChanges();
    }

    // ── sorting ───────────────────────────────────────────────────────────────

    private static void sortPool(List<ItemStack> pool) {
        pool.sort(
            Comparator.comparingInt(InventorySorter::categoryOrder)
                .thenComparing(InventorySorter::subSortKey)
                .thenComparingInt(s -> -tierScore(s))
                .thenComparingInt(s -> -enchantCount(s))
        );
    }

    /**
     * Primary category buckets:
     *   0   weapons
     *   1   tools
     *   2   armor
     *   3   food
     *   4   ores (ore blocks + raw ores + ancient debris)
     *   5   plants (saplings, flowers, leaves, bamboo plant, crops, etc.)
     *  100+ wood items, one bucket per wood type (100=oak, 101=spruce, …)
     *  200  non-wood building blocks
     *  300  misc
     */
    static int categoryOrder(ItemStack stack) {
        if (isWeapon(stack)) return 0;
        if (isTool(stack))   return 1;
        if (isArmor(stack))  return 2;
        if (isFood(stack))   return 3;
        if (isOre(stack))    return 4;
        if (isPlant(stack))  return 5;
        int wood = woodTypeIndex(stack);
        if (wood >= 0) return 100 + wood;
        if (stack.getItem() instanceof BlockItem) return 200;
        return 300;
    }

    /**
     * Secondary sort key within the same category.
     * Alphabetical by item registry path with one normalization:
     * deepslate ores are sorted immediately after their surface counterparts
     * so coal_ore and deepslate_coal_ore end up adjacent.
     */
    static String subSortKey(ItemStack stack) {
        String id = itemPath(stack);
        if (id.startsWith("deepslate_") && id.endsWith("_ore")) {
            // "deepslate_coal_ore" → sort as "coal_ore~" (tilde > letters, after "coal_ore")
            return id.substring("deepslate_".length()) + "~";
        }
        return id;
    }

    // ── category predicates ───────────────────────────────────────────────────

    private static boolean isWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return stack.is(ItemTags.SWORDS)
            || item == Items.MACE
            || item == Items.BOW
            || item == Items.CROSSBOW
            || item == Items.TRIDENT;
    }

    private static boolean isTool(ItemStack stack) {
        Item item = stack.getItem();
        return stack.is(ItemTags.PICKAXES)
            || stack.is(ItemTags.AXES)
            || stack.is(ItemTags.SHOVELS)
            || stack.is(ItemTags.HOES)
            || item == Items.SHEARS
            || item == Items.FLINT_AND_STEEL
            || item == Items.FISHING_ROD;
    }

    private static boolean isArmor(ItemStack stack) {
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        return eq != null && isArmorSlot(eq.slot());
    }

    private static boolean isFood(ItemStack stack) {
        return stack.get(DataComponents.FOOD) != null;
    }

    private static boolean isOre(ItemStack stack) {
        String id = itemPath(stack);
        return id.endsWith("_ore")
            || id.equals("ancient_debris")
            || (id.startsWith("raw_") && (id.contains("iron") || id.contains("copper") || id.contains("gold")));
    }

    private static boolean isPlant(ItemStack stack) {
        Item item = stack.getItem();
        return stack.is(ItemTags.SAPLINGS)
            || stack.is(ItemTags.SMALL_FLOWERS)
            || stack.is(ItemTags.LEAVES)
            || item == Items.SUNFLOWER
            || item == Items.LILAC
            || item == Items.ROSE_BUSH
            || item == Items.PEONY
            || item == Items.TALL_GRASS
            || item == Items.LARGE_FERN
            || item == Items.PITCHER_PLANT
            || item == Items.WHEAT_SEEDS
            || item == Items.PUMPKIN_SEEDS
            || item == Items.MELON_SEEDS
            || item == Items.BEETROOT_SEEDS
            || item == Items.TORCHFLOWER_SEEDS
            || item == Items.PITCHER_POD
            || item == Items.BAMBOO
            || item == Items.CACTUS
            || item == Items.SUGAR_CANE
            || item == Items.KELP
            || item == Items.LILY_PAD
            || item == Items.VINE
            || item == Items.NETHER_WART
            || item == Items.MOSS_BLOCK
            || item == Items.MOSS_CARPET
            || item == Items.AZALEA
            || item == Items.FLOWERING_AZALEA
            || item == Items.SPORE_BLOSSOM
            || item == Items.RED_MUSHROOM
            || item == Items.BROWN_MUSHROOM
            || item == Items.RED_MUSHROOM_BLOCK
            || item == Items.BROWN_MUSHROOM_BLOCK
            || item == Items.MUSHROOM_STEM;
    }

    /**
     * Returns the index into WOOD_TYPES (0=oak, 1=spruce, …) or -1 if not a wood item.
     * Plants like saplings and bamboo-the-item are already caught by isPlant and never reach here.
     */
    private static int woodTypeIndex(ItemStack stack) {
        String id = itemPath(stack);
        // Normalize "stripped_" prefix so stripped_oak_log → oak_log
        if (id.startsWith("stripped_")) id = id.substring("stripped_".length());
        for (int i = 0; i < WOOD_TYPES.size(); i++) {
            String wood = WOOD_TYPES.get(i);
            if (id.startsWith(wood + "_") || id.equals(wood)) return i;
        }
        return -1;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static String itemPath(ItemStack stack) {
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null ? key.getPath() : "";
    }

    private static ItemStack findBestForLayout(List<ItemStack> pool, String savedId) {
        Identifier id = Identifier.tryParse(savedId);
        if (id == null) return null;
        Item savedItem = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (savedItem == null || savedItem == Items.AIR) return null;

        TagKey<Item> toolTag = toolTagFor(savedItem);
        if (toolTag != null) {
            return pool.stream()
                .filter(s -> s.is(toolTag))
                .max(Comparator.comparingInt(InventorySorter::tierScore)
                    .thenComparingInt(InventorySorter::enchantCount))
                .orElse(null);
        }

        ItemStack dummy = new ItemStack(savedItem);
        return pool.stream()
            .filter(s -> ItemStack.isSameItem(s, dummy))
            .findFirst()
            .orElse(null);
    }

    private static TagKey<Item> toolTagFor(Item item) {
        ItemStack dummy = new ItemStack(item);
        if (dummy.is(ItemTags.PICKAXES)) return ItemTags.PICKAXES;
        if (dummy.is(ItemTags.AXES))     return ItemTags.AXES;
        if (dummy.is(ItemTags.SHOVELS))  return ItemTags.SHOVELS;
        if (dummy.is(ItemTags.HOES))     return ItemTags.HOES;
        if (dummy.is(ItemTags.SWORDS))   return ItemTags.SWORDS;
        return null;
    }

    private static ItemStack findBestArmor(List<ItemStack> pool, EquipmentSlot slot) {
        return pool.stream()
            .filter(s -> {
                Equippable eq = s.get(DataComponents.EQUIPPABLE);
                return eq != null && eq.slot() == slot;
            })
            .max(Comparator.comparingInt(InventorySorter::tierScore)
                .thenComparingInt(InventorySorter::enchantCount))
            .orElse(null);
    }

    private static int armorInventorySlot(EquipmentSlot slot) {
        return switch (slot) {
            case FEET  -> 36;
            case LEGS  -> 37;
            case CHEST -> 38;
            case HEAD  -> 39;
            default    -> -1;
        };
    }

    private static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST
            || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    static int tierScore(ItemStack stack) { return stack.getMaxDamage(); }

    static int enchantCount(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).size();
    }
}
