# Rotatable Hoppers — Design Spec
**Date:** 2026-05-13
**Target:** Minecraft 1.21.x, Fabric

---

## Overview

A standalone Fabric mod that allows any placed hopper to be rotated to all 6 cardinal directions (DOWN, NORTH, EAST, SOUTH, WEST, UP) by right-clicking with a pickaxe. The hopper pushes items toward its facing direction and pulls from the opposite side — a fully rotatable item pipe. No config, no new items, no crafting recipe. Works on all existing hoppers in the world.

---

## Tech Stack

| Dependency | Purpose |
|---|---|
| Fabric Loader + Fabric API | Mod loader, events, blockstate |

No Cloth Config or Mod Menu — nothing to configure.

---

## Project Structure

```
src/main/java/com/jeefbeebos23/rotatable_hoppers/
├── RotatableHoppers.java               ← ModInitializer entry point (registers nothing; mixins do all work)
└── mixin/
    ├── HopperBlockMixin.java           ← Extends FACING to include UP, intercepts right-click with pickaxe
    └── HopperBlockEntityMixin.java     ← Overrides pull-from logic to use FACING.getOpposite()

src/main/resources/
├── fabric.mod.json
├── rotatable_hoppers.mixins.json
└── assets/
    ├── minecraft/blockstates/hopper.json           ← Overrides vanilla blockstate to add facing=up variant
    └── rotatable_hoppers/models/block/hopper_up.json ← Vanilla hopper model with x:180 rotation (inverted)
```

---

## Architecture

All behavior lives in two mixins. No new blocks, block entities, or items are registered. The mod piggybacks entirely on the existing `HopperBlock` and `HopperBlockEntity`.

---

## Components

### HopperBlockMixin

**Target:** `net.minecraft.world.level.block.HopperBlock`

**Blockstate extension:**
- Vanilla `FACING` accepts `Direction.DOWN` + 4 horizontals (5 total).
- Mixin injects into `createBlockStateDefinition` to replace the property with one that also accepts `Direction.UP`.
- Existing world saves with the 5 vanilla directions load unchanged.

**Right-click rotation:**
- Inject into `useItemOn` (Mojmap name; the method Block receives when a player right-clicks with an item in hand).
- Condition: the item used is an instance of `PickaxeItem` (covers all pickaxe tiers).
- On match: read current `FACING` from blockstate, advance to next in cycle `DOWN → NORTH → EAST → SOUTH → WEST → UP → DOWN`, set new blockstate on the world, return `InteractionResult.SUCCESS`.
- No sneaking required. Does not consume the pickaxe's durability.

### HopperBlockEntityMixin

**Target:** `net.minecraft.world.level.block.entity.HopperBlockEntity`

**Pull-from override:**
- Vanilla always pulls from the container (or item entities) directly above the hopper.
- Mixin injects into the method that determines the "input" container position.
- New logic: pull from the block at `pos.relative(facing.getOpposite())`, where `facing` is read from the blockstate.
- For `FACING = DOWN` (vanilla default), `getOpposite()` = UP, which matches vanilla behavior exactly — no regression.

**Push-to (unchanged):**
- Vanilla already uses `FACING` to determine the output container. No change needed.

### Model

**`assets/rotatable_hoppers/models/block/hopper_up.json`**
```json
{
  "parent": "minecraft:block/hopper",
  "x": 180
}
```
The vanilla hopper model (funnel on top, output at bottom) inverted 180° on the X axis gives a hopper with funnel on the bottom and output pointing up — correct for `FACING = UP`.

**`assets/minecraft/blockstates/hopper.json`**
Placed under `assets/minecraft/` so Fabric loads it as a vanilla asset override. Contains all 6 variants (the original 5 copied verbatim, plus `facing=up` mapped to `rotatable_hoppers:block/hopper_up`). Must be a complete file — partial overrides are not supported.

---

## Data Flow

1. Player right-clicks hopper with pickaxe → `HopperBlockMixin` reads `FACING`, steps to next direction, writes new blockstate → model updates client-side immediately.
2. On each hopper tick: `HopperBlockEntityMixin` reads `FACING` → pull target = `pos.relative(FACING.getOpposite())` → push target = `pos.relative(FACING)` (vanilla, unchanged).
3. All state is in the blockstate `FACING` property. No NBT, no saved data, no migration.

---

## Error Handling

- If no container exists at the pull or push position, the hopper idles — same as vanilla behavior when facing air.
- `FACING = DOWN` with the new pull logic (`getOpposite()` = UP) is identical to vanilla. Zero regression for unrotated hoppers.
- Existing world saves load without issue — only DOWN and the 4 horizontals were ever stored; the UP variant is additive.

---

## Build

- Gradle + Fabric Loom
- Java 21
- Output: single JAR in `build/libs/`
- Repo: standalone git repository at `coding_projects/rotatable-hoppers/`
