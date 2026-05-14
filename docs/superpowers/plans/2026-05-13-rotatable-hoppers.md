# Rotatable Hoppers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a standalone Fabric 1.21.4 mod that lets players right-click any hopper with a pickaxe to cycle its facing direction through all 6 cardinal directions; the hopper pulls from the opposite side and pushes toward the faced direction.

**Architecture:** Two Mixins on vanilla classes — `HopperBlockMixin` extends the FACING blockstate property to include UP and intercepts right-click to cycle directions; `HopperBlockEntityMixin` redirects the pull-source lookup to use `facing.getOpposite()` instead of always-above. No new blocks, items, or config.

**Tech Stack:** Java 21, Fabric Loader 0.16.x, Fabric API 0.119.x, Gradle 8 + Fabric Loom 1.9, Yarn mappings 1.21.4

---

## File Map

| File | Purpose |
|---|---|
| `build.gradle` | Loom project config + dependencies |
| `gradle.properties` | Pinned MC/Fabric versions |
| `settings.gradle` | Project name |
| `src/main/resources/fabric.mod.json` | Mod metadata + entrypoints |
| `src/main/resources/rotatable_hoppers.mixins.json` | Mixin class registry |
| `src/main/java/.../RotatableHoppers.java` | Empty `ModInitializer` entry point |
| `src/main/java/.../mixin/HopperBlockMixin.java` | FACING extension + right-click rotation |
| `src/main/java/.../mixin/HopperBlockEntityMixin.java` | Pull-direction redirect |
| `src/main/resources/assets/minecraft/blockstates/hopper.json` | Vanilla blockstate override adding `facing=up` variants |

All source under `src/main/java/com/jeefbeebos23/rotatable_hoppers/` (abbreviated below).

---

## Task 1: Scaffold Repo

**Files:**
- Create: `C:\Users\wbgui\coding_projects\rotatable-hoppers\` (new standalone repo)
- Create: `settings.gradle`
- Create: `gradle.properties`
- Create: `build.gradle`

- [ ] **Step 1: Create the directory and initialize git**

```powershell
New-Item -ItemType Directory "C:\Users\wbgui\coding_projects\rotatable-hoppers"
cd "C:\Users\wbgui\coding_projects\rotatable-hoppers"
git init
git remote add origin https://github.com/jeefbeebos23/rotatable-hoppers.git
```

- [ ] **Step 2: Create `settings.gradle`**

```groovy
pluginManagement {
    repositories {
        maven { url 'https://maven.fabricmc.net/' }
        gradlePluginPortal()
    }
}
rootProject.name = 'rotatable-hoppers'
```

- [ ] **Step 3: Create `gradle.properties`**

> Verify exact patch versions at https://fabricmc.net/develop/ if the build fails to resolve artifacts.

```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
minecraft_version=1.21.4
yarn_mappings=1.21.4+build.8
loader_version=0.16.10
fabric_version=0.119.2+1.21.4
```

- [ ] **Step 4: Create `build.gradle`**

```groovy
plugins {
    id 'fabric-loom' version '1.9-SNAPSHOT'
}

version = '1.0.0'
group = 'com.jeefbeebos23'

base {
    archivesName = 'rotatable-hoppers'
}

repositories {}

dependencies {
    minecraft "com.mojang:minecraft:${minecraft_version}"
    mappings "net.fabricmc:yarn:${yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${fabric_version}"
}

processResources {
    inputs.property "version", project.version
    filteringCharset "UTF-8"
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = 21
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
```

- [ ] **Step 5: Generate the Gradle wrapper**

```powershell
gradle wrapper --gradle-version 8.11.1
```

Expected: creates `gradlew`, `gradlew.bat`, `gradle/wrapper/` directory.

- [ ] **Step 6: Create `.gitignore`**

```
.gradle/
build/
run/
.idea/
*.iml
out/
```

- [ ] **Step 7: Commit scaffold**

```bash
git add .
git commit -m "chore: scaffold Fabric 1.21.4 project"
```

---

## Task 2: Project Skeleton

**Files:**
- Create: `src/main/resources/fabric.mod.json`
- Create: `src/main/resources/rotatable_hoppers.mixins.json`
- Create: `src/main/java/com/jeefbeebos23/rotatable_hoppers/RotatableHoppers.java`

- [ ] **Step 1: Create the source directory tree**

```powershell
New-Item -ItemType Directory -Force "src/main/java/com/jeefbeebos23/rotatable_hoppers/mixin"
New-Item -ItemType Directory -Force "src/main/resources/assets/minecraft/blockstates"
```

- [ ] **Step 2: Create `src/main/resources/fabric.mod.json`**

```json
{
  "schemaVersion": 1,
  "id": "rotatable_hoppers",
  "version": "${version}",
  "name": "Rotatable Hoppers",
  "description": "Right-click any hopper with a pickaxe to rotate it in all 6 directions.",
  "authors": ["jeefbeebos23"],
  "license": "MIT",
  "environment": "*",
  "entrypoints": {
    "main": ["com.jeefbeebos23.rotatable_hoppers.RotatableHoppers"]
  },
  "mixins": ["rotatable_hoppers.mixins.json"],
  "depends": {
    "fabricloader": ">=0.16.0",
    "fabric-api": "*",
    "minecraft": "~1.21.4"
  }
}
```

- [ ] **Step 3: Create `src/main/resources/rotatable_hoppers.mixins.json`**

```json
{
  "required": true,
  "package": "com.jeefbeebos23.rotatable_hoppers.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [
    "HopperBlockMixin",
    "HopperBlockEntityMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

- [ ] **Step 4: Create `RotatableHoppers.java`**

```java
package com.jeefbeebos23.rotatable_hoppers;

import net.fabricmc.api.ModInitializer;

public class RotatableHoppers implements ModInitializer {
    @Override
    public void onInitialize() {
        // All behavior is in mixins; nothing to register here.
    }
}
```

- [ ] **Step 5: Verify the project compiles**

```powershell
.\gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`. No source files to compile yet, but the Gradle config and resource processing must resolve cleanly.

- [ ] **Step 6: Commit skeleton**

```bash
git add .
git commit -m "feat: add project skeleton and entry point"
```

---

## Task 3: HopperBlockMixin — Extend FACING + Right-Click Rotation

**Files:**
- Create: `src/main/java/com/jeefbeebos23/rotatable_hoppers/mixin/HopperBlockMixin.java`

**Context:** Vanilla `HopperBlock.FACING` is a `DirectionProperty` that holds 5 values: DOWN + 4 horizontals. We extend it to 6 by replacing the static field via a `<clinit>` tail injection. This runs before any `HopperBlock` instance is constructed (block registration happens in `Bootstrap.bootStrap()`, well after class load), so `appendProperties` picks up the new 6-direction property correctly.

The right-click handler injects into `onUseWithItem` (Yarn 1.21.4). In 1.21.4 this method returns `ItemActionResult`. Verify the exact descriptor against decompiled source if injection fails; the method name in Yarn may be `onUseWithItem` or similar.

- [ ] **Step 1: Create `HopperBlockMixin.java`**

```java
package com.jeefbeebos23.rotatable_hoppers.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlock.class)
public class HopperBlockMixin {

    @Shadow @Final @Mutable
    public static DirectionProperty FACING;

    // Replace FACING after vanilla's static initializer sets it, adding Direction.UP.
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addUpDirection(CallbackInfo ci) {
        FACING = DirectionProperty.of("facing",
            Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP);
    }

    // Intercept right-click with any pickaxe to cycle the hopper's facing direction.
    @Inject(method = "onUseWithItem", at = @At("HEAD"), cancellable = true)
    private void rotateFacing(ItemStack stack, BlockState state, World world, BlockPos pos,
                               PlayerEntity player, Hand hand, BlockHitResult hit,
                               CallbackInfoReturnable<ItemActionResult> cir) {
        if (!(stack.getItem() instanceof PickaxeItem)) return;
        if (!world.isClient()) {
            Direction next = nextFacing(state.get(HopperBlock.FACING));
            world.setBlockState(pos, state.with(HopperBlock.FACING, next));
        }
        cir.setReturnValue(ItemActionResult.sidedSuccess(world.isClient()));
    }

    private static Direction nextFacing(Direction d) {
        return switch (d) {
            case DOWN  -> Direction.NORTH;
            case NORTH -> Direction.EAST;
            case EAST  -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST  -> Direction.UP;
            case UP    -> Direction.DOWN;
        };
    }
}
```

- [ ] **Step 2: Compile to verify no errors**

```powershell
.\gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`. Fix any import or method-name mismatches against the decompiled source if the build fails.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/jeefbeebos23/rotatable_hoppers/mixin/HopperBlockMixin.java
git commit -m "feat: extend hopper FACING to include UP, add pickaxe right-click rotation"
```

---

## Task 4: HopperBlockEntityMixin — Redirect Pull Direction

**Files:**
- Create: `src/main/java/com/jeefbeebos23/rotatable_hoppers/mixin/HopperBlockEntityMixin.java`

**Context:** Vanilla `HopperBlockEntity` has a private static method `getInputInventory(World, Hopper)` that returns the inventory to pull from. In vanilla it always uses the position directly above the hopper. We inject at HEAD of this method (cancellable) and return the inventory at `facing.getOpposite()` instead.

Key: for `FACING = DOWN` (vanilla default), `getOpposite()` = UP — identical to vanilla, so unrotated hoppers behave exactly as before.

`HopperBlockEntity` implements `Hopper` and extends `BlockEntity`, so casting to `BlockEntity` gives us `getPos()`. `getInventoryAt(World, BlockPos)` is a private static we shadow.

> **Verify in decompiled source:** Open `HopperBlockEntity` and confirm the private static method names are `getInputInventory` and `getInventoryAt`. If they differ (obfuscation variant), adjust the `method` descriptor strings accordingly.

- [ ] **Step 1: Create `HopperBlockEntityMixin.java`**

```java
package com.jeefbeebos23.rotatable_hoppers.mixin;

import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.util.math.Hopper;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    // Shadow the private static helper that looks up an inventory at a block position.
    @Shadow
    private static @Nullable Inventory getInventoryAt(World world, BlockPos pos) {
        throw new AssertionError("mixin shadow");
    }

    // Replace vanilla's "always pull from above" with "pull from facing.getOpposite()".
    @Inject(method = "getInputInventory", at = @At("HEAD"), cancellable = true)
    private static void redirectPullDirection(World world, Hopper hopper,
                                              CallbackInfoReturnable<@Nullable Inventory> cir) {
        if (!(hopper instanceof BlockEntity be)) return;
        BlockPos pos = be.getPos();
        Direction facing = world.getBlockState(pos).get(HopperBlock.FACING);
        cir.setReturnValue(getInventoryAt(world, pos.offset(facing.getOpposite())));
    }
}
```

- [ ] **Step 2: Compile**

```powershell
.\gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`. If `Hopper` import fails, try `net.minecraft.block.entity.Hopper` or `net.minecraft.util.math.Hopper` — verify the correct Yarn package from decompiled source.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/jeefbeebos23/rotatable_hoppers/mixin/HopperBlockEntityMixin.java
git commit -m "feat: redirect hopper pull direction to use facing.getOpposite()"
```

---

## Task 5: Blockstate Asset Override

**Files:**
- Create: `src/main/resources/assets/minecraft/blockstates/hopper.json`

**Context:** Fabric mods can override vanilla assets by placing files under `assets/minecraft/`. The blockstate file must be **complete** — it must include all existing vanilla variants verbatim, plus the new `facing=up` entries. Partial overrides are not supported.

The UP variant reuses `minecraft:block/hopper` (the down-facing model) with `"x": 180`, which flips it: funnel points down (the new input/pull side), output tube points up (the new output/push side).

- [ ] **Step 1: Create `src/main/resources/assets/minecraft/blockstates/hopper.json`**

```json
{
  "variants": {
    "facing=down,enabled=false":  { "model": "minecraft:block/hopper" },
    "facing=down,enabled=true":   { "model": "minecraft:block/hopper" },
    "facing=east,enabled=false":  { "model": "minecraft:block/hopper_side", "y": 270 },
    "facing=east,enabled=true":   { "model": "minecraft:block/hopper_side", "y": 270 },
    "facing=north,enabled=false": { "model": "minecraft:block/hopper_side" },
    "facing=north,enabled=true":  { "model": "minecraft:block/hopper_side" },
    "facing=south,enabled=false": { "model": "minecraft:block/hopper_side", "y": 180 },
    "facing=south,enabled=true":  { "model": "minecraft:block/hopper_side", "y": 180 },
    "facing=west,enabled=false":  { "model": "minecraft:block/hopper_side", "y": 90 },
    "facing=west,enabled=true":   { "model": "minecraft:block/hopper_side", "y": 90 },
    "facing=up,enabled=false":    { "model": "minecraft:block/hopper", "x": 180 },
    "facing=up,enabled=true":     { "model": "minecraft:block/hopper", "x": 180 }
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/assets/minecraft/blockstates/hopper.json
git commit -m "feat: add facing=up blockstate variants for hopper (inverted model)"
```

---

## Task 6: Build, Deploy, and Test

**Files:** None new — JAR output at `build/libs/rotatable-hoppers-1.0.0.jar`

- [ ] **Step 1: Build the JAR**

```powershell
.\gradlew build
```

Expected: `BUILD SUCCESSFUL`. JAR created at `build/libs/rotatable-hoppers-1.0.0.jar`.

- [ ] **Step 2: Copy to Minecraft mods folder**

```powershell
Copy-Item "build/libs/rotatable-hoppers-1.0.0.jar" "C:\Users\wbgui\AppData\Roaming\.minecraft\mods\"
```

- [ ] **Step 3: Launch Minecraft with Fabric and run these manual tests**

**Test A — Default behavior unchanged:**
1. Place a hopper. Confirm it faces DOWN (vanilla default).
2. Place a chest directly above. Put items in the chest.
3. Confirm items flow down through the hopper into a container below. ✓ No regression.

**Test B — Right-click rotation:**
1. Hold any pickaxe.
2. Right-click the hopper. Confirm facing cycles: DOWN → NORTH → EAST → SOUTH → WEST → UP → DOWN.
3. Confirm the hopper model updates visually on each click.
4. Confirm cycling wraps around from UP back to DOWN.

**Test C — Rotated transfer (horizontal):**
1. Place hopper facing EAST.
2. Place a chest to the WEST of the hopper (opposite side).
3. Place a chest to the EAST of the hopper (facing side).
4. Put items in the west chest. Confirm items transfer east through the hopper. ✓

**Test D — Upward transfer:**
1. Place hopper facing UP.
2. Place a chest below the hopper (south = opposite of UP).
3. Place a chest above the hopper (output side).
4. Put items in the lower chest. Confirm items transfer upward. ✓

**Test E — Visual check for UP:**
1. Place a hopper facing UP. Confirm the model renders as an inverted hopper (funnel pointing down, tube pointing up). ✓

- [ ] **Step 4: Push to GitHub**

```bash
git push -u origin master
```

---

## Self-Review

**Spec coverage:**
- ✓ All 6 directions supported via `<clinit>` injection on `FACING`
- ✓ Right-click with pickaxe cycles directions (Task 3)
- ✓ Pull from `facing.getOpposite()` (Task 4)
- ✓ Push toward `facing` — vanilla already does this via `FACING`, no change needed
- ✓ UP model via `x: 180` in blockstate override (Task 5)
- ✓ DOWN default matches vanilla exactly (`getOpposite()` of DOWN = UP)
- ✓ No config, no new items, no registration in entry point

**Placeholder scan:** None found.

**Type consistency:**
- `HopperBlock.FACING` referenced in both mixins — same static field, consistent
- `Direction.nextFacing()` switch is exhaustive (all 6 cases)
- `getInventoryAt(World, BlockPos)` shadowed and called in one place only
