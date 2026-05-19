# QoL Mod

A quality-of-life Fabric mod for Minecraft 26.1.2 that bundles a large number of small improvements into one configurable package. All features can be toggled individually in-game via the config screen (default keybind: **K**).

## Features

### Mining & Farming

- **Vein Miner** — Hold the vein miner key while breaking an ore or log to break the entire connected vein/tree at once. Configurable block list and max block count (up to 2048).
- **Auto Tree Replant** — Automatically replants a sapling when you break the base of a tree. Supports 1×1 and 2×2 trees (spruce, dark oak, jungle).
- **Auto Crop Replant** — Right-click a fully-grown crop with a hoe to harvest it and instantly replant. Drops loot and resets the crop to age 0.
- **Fast Leaf Decay** — Leaves decay immediately after a tree is felled instead of waiting several seconds.
- **Fortune on Extra Crops** — Fortune enchantment now applies bonus drops to crops and blocks not normally affected: pumpkins, sugarcane, bamboo, cactus, mature nether wart, and mature cocoa beans.
- **Bonemeal for Extra Plants** — Bonemeal now works on plants that vanilla ignores: sugarcane (grows one block taller, up to height 3), cactus (same), nether wart (advances one age stage), and sweet berry bushes (advances one age stage).

### Inventory

- **Quick Stack** — Button in the inventory screen that pushes matching items from your inventory into nearby chests within a configurable radius (default 10 blocks).
- **Restock** — Button that pulls items from nearby chests to refill stacks in your inventory that have run low.
- **Inventory Sort** — Sorts your inventory, grouping items by type. Respects a saved hotbar layout.
- **Save Layout** — Saves the current contents of your hotbar (including offhand) as a template that Sort restores.
- **Auto-Refill Hotbar/Offhand** — When a hotbar or offhand stack runs out, automatically pulls a replacement from your inventory.
- **Mouse Tweaks** — Right-drag to distribute items one-at-a-time across slots; left-drag to distribute evenly.
- **Magic Mirror** — A small bed-icon button near the recipe book toggle in the inventory screen. Click it to instantly teleport to your respawn point (bed or respawn anchor), just like Terraria's Magic Mirror.

### Crafting & Furnace

- **Craft Slabs → Blocks** — Adds a 2×1 recipe to convert two slabs of the same type back into a full block.
- **Furnace XP Display** — Shows accumulated XP stored in a furnace and adds a "Collect XP" button to retrieve it without smelting.

### Combat & Enchanting

- **Infinity Bow (all arrows)** — Infinity works with any arrow type, not just regular arrows.
- **Remove Enchant Level Cap** — Removes the anvil's "Too Expensive" limit so you can apply any number of enchantments.
- **Mending + Infinity on Bows** — Allows Mending and Infinity to coexist on the same bow.

### Villager

- **Library Villager** — A special villager type that sells enchanted books. Uses a Mystery Book item: right-click it to open a screen where you choose which enchantment and level you want. The book is then created with that enchantment.
- **Disable Villager Restock** — Prevents villager trades from restocking, making trade supplies truly finite.

### Movement & Visual

- **Zoom** — Hold the zoom key to zoom in (like OptiFine's zoom). Configurable keybind.
- **Disable Elytra Durability** — Elytra no longer takes durability damage while flying.

## Configuration

Open the config screen with the **K** key (rebindable) or through Mod Menu. Each feature has its own toggle. Some features have additional sliders or lists (e.g., vein miner block list, auto-stack radius).

## Requirements

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.148.0+26.1.2
- Cloth Config 2 (for the config screen)
