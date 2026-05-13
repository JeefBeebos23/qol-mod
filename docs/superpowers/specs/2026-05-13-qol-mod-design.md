# QoL Mod — Design Spec
**Date:** 2026-05-13
**Target:** Minecraft 1.21.x, Fabric

---

## Overview

A single Fabric mod that bundles 9 quality-of-life features into one JAR. Each feature can be toggled independently via an in-game config screen (Cloth Config + Mod Menu). Features default to enabled. No feature depends on another.

---

## Tech Stack

| Dependency | Purpose |
|---|---|
| Fabric Loader + Fabric API | Mod loader, events, keybinds |
| Cloth Config API | Config serialization + in-game GUI |
| Mod Menu | Adds mod to Mods list with a config button |

---

## Project Structure

```
src/main/java/com/jeefbeebos23/qolmod/
├── QolMod.java                     ← ModInitializer entry point
├── config/
│   └── QolConfig.java              ← All settings, Cloth Config screen builder
├── features/
│   ├── ZoomFeature.java
│   ├── VeinMinerFeature.java
│   ├── AutoStackFeature.java
│   ├── ElytraFeature.java
│   ├── InfinityBowFeature.java
│   ├── MouseTweaksFeature.java
│   ├── SlabRecipeFeature.java
│   ├── EnchantLimitFeature.java
│   └── FurnaceXpFeature.java
└── mixin/
    ├── ElytraMixin.java
    ├── InfinityBowMixin.java
    ├── EnchantMixin.java
    ├── FurnaceScreenMixin.java
    └── InventoryScreenMixin.java

src/main/resources/
├── fabric.mod.json
├── qolmod.mixins.json
└── assets/qolmod/lang/en_us.json
```

Each `Feature` class registers its own keybinds and event listeners in a `register()` method called from `QolMod.java`. `QolConfig` is a singleton read by all features before applying behavior.

---

## Config System

**Storage location:** `.minecraft/config/qolmod.json`
**Access:** Mod Menu → QoL Mod → Config button

Screen layout (grouped by category):

```
┌─────────────────────────────────────┐
│          QoL Mod Settings           │
├─────────────────────────────────────┤
│  Movement & Visual                  │
│    [✓] Zoom                         │
│    [✓] Elytra Durability            │
│                                     │
│  Mining                             │
│    [✓] Vein Miner                   │
│        Max blocks: [64]             │
│        Block list: [Edit...]        │
│                                     │
│  Inventory                          │
│    [✓] Auto-Stack to Chests         │
│        Radius: [10]                 │
│    [✓] Mouse Tweaks                 │
│                                     │
│  Combat & Enchanting                │
│    [✓] Infinity Bow (all arrows)    │
│    [✓] Remove Enchant Limit         │
│                                     │
│  Crafting & Furnace                 │
│    [✓] Craft Slabs → Blocks         │
│    [✓] Furnace XP Display           │
└─────────────────────────────────────┘
```

---

## Features

### 1. Zoom
- **Keybind:** `C` (default, rebindable)
- **Behavior:** While key is held, smoothly reduces FOV. Releases back to normal on key release.
- **Implementation:** Fabric keybind registry + client render tick event to interpolate FOV override.
- **Config toggle:** `zoomEnabled` (boolean)

### 2. Vein Miner
- **Keybind:** `~` (default, rebindable)
- **Behavior:** While key is held, breaking a block triggers a flood-fill to find all connected blocks of the same type. Mines them all, up to `veinMinerMaxBlocks` limit.
- **Default block list:** All vanilla ores (`minecraft:coal_ore`, `minecraft:iron_ore`, etc. including deepslate variants) + all log types.
- **Block list editing:** Config GUI exposes a string list field for adding/removing block IDs.
- **Implementation:** `BlockBreakCallback` event. Flood-fill runs server-side via a recursive BFS capped at max blocks.
- **Config:** `veinMinerEnabled` (boolean), `veinMinerMaxBlocks` (int, default 64), `veinMinerBlocks` (string list)

### 3. Auto-Stack to Chests
- **Trigger:** Button injected into the player inventory screen (bottom-right area, labeled "Quick Stack").
- **Behavior:** On click, scans all chests within `autoStackRadius` blocks of the player. For each chest, pushes inventory items that match item types already present in that chest.
- **Scope:** Matches by item type only (not NBT). Does not move items to empty chests.
- **Implementation:** `InventoryScreenMixin` injects the button. Click handler iterates nearby block entities, filters for `ChestBlockEntity`, compares inventories.
- **Config:** `autoStackEnabled` (boolean), `autoStackRadius` (int, default 10)

### 4. Elytra Durability Toggle
- **Behavior:** When enabled, elytra takes no durability damage while flying.
- **Implementation:** `ElytraMixin` cancels the damage call in `ElytraItem`'s tick logic.
- **Config:** `elytraDurabilityEnabled` (boolean, default true = durability off)

### 5. Infinity Bow with All Arrows
- **Behavior:** A bow enchanted with Infinity consumes no arrows of any type (tipped, spectral, regular).
- **Implementation:** `InfinityBowMixin` removes the arrow-type check that restricts Infinity to `minecraft:arrow` only.
- **Config:** `infinityBowEnabled` (boolean)

### 6. Mouse Tweaks
- **Behavior (no keybind required):**
  - Scroll wheel over a slot while holding an item moves items one at a time into/out of that slot.
  - Shift + right-drag across slots distributes the held stack evenly across hovered slots.
- **Implementation:** `InventoryScreenMixin` intercepts `mouseScrolled` and `mouseDragged` events.
- **Config:** `mouseTweaksEnabled` (boolean)

### 7. Craft Slabs Back into Blocks
- **Behavior:** 2 of the same slab in any crafting grid yields 1 full block of that material.
- **Scope:** All vanilla slabs (oak, stone, cobblestone, brick, etc.).
- **Implementation:** Registers shapeless recipes at mod init using Fabric's `RecipeManager` event / data-driven recipes via `data/qolmod/recipes/`.
- **Config:** `slabRecipeEnabled` (boolean)

### 8. Remove Enchantment Level Limit
- **Behavior:** Removes the "Too Expensive!" block on anvils and removes the cap on enchantment levels. Does not add a way to obtain higher-level enchants — only removes the anvil's refusal to apply them.
- **Implementation:** `EnchantMixin` targets the anvil cost calculation to remove the 40-level cap and the "too expensive" flag.
- **Config:** `enchantLimitEnabled` (boolean)

### 9. Furnace XP Display & Extraction
- **Behavior:**
  - The furnace screen shows a numeric overlay of stored XP (e.g., "Stored XP: 142").
  - A small clickable button ("Collect XP") spawns XP orbs at the furnace's world position without removing the furnace or its contents.
- **Implementation:** `FurnaceScreenMixin` renders the XP text overlay and injects the Collect button. Click handler calls `AbstractFurnaceBlockEntity`'s XP reward logic.
- **Config:** `furnaceXpEnabled` (boolean)

---

## Error Handling

- If Cloth Config or Mod Menu are absent at runtime, the mod logs a warning and falls back to config file only (no crash).
- Vein miner flood-fill is hard-capped at `veinMinerMaxBlocks` to prevent server lag.
- Auto-stack gracefully skips locked/double-chest halves already being iterated.

---

## Build

- Gradle + Fabric Loom
- Java 21
- Output: single JAR in `build/libs/`
