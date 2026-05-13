# QoL Mod — Download & Install Guide

A personal Minecraft QoL mod with 9 quality-of-life features, all toggleable in-game.

## Features

- **Zoom** — hold a key to zoom in (like OptiFine)
- **Vein Miner** — hold a key to mine an entire ore vein at once
- **Auto-Stack to Chests** — button in your inventory to quickly push items to nearby chests
- **Elytra Durability** — toggle elytra durability loss on/off
- **Infinity Bow (All Arrows)** — Infinity enchantment works on tipped and spectral arrows too
- **Mouse Tweaks** — scroll over a slot to quick-move it; shift+right-drag to spread items
- **Slab → Block Recipe** — craft two slabs of the same type back into a full block
- **Remove Enchant Limit** — removes the "Too Expensive!" cap in the anvil
- **Furnace XP Display** — see stored XP in furnaces and collect it with a button

---

## Which version do I need?

| Minecraft Version | Mod File |
|---|---|
| **26.1.2** (latest) | `qolmod-1.1.0.jar` |
| **1.21.4** | `qolmod-1.0.0.jar` |

Both files are in this folder on GitHub.

---

## What You Need to Download

You need **3 files** total. Download them all before starting.

### 1. Minecraft with Fabric Loader

Fabric is the mod loader that makes this mod work. If you've never used Fabric before:

1. Go to **https://fabricmc.net/use/installer/**
2. Download the installer for your OS (Windows: click "Download For Windows")
3. Run the installer
4. Make sure "Minecraft Version" is set to your version (**26.1.2** or **1.21.4**)
5. Click Install
6. Open the Minecraft Launcher — you'll see a new Fabric profile. Select it and launch once to let it set up.

### 2. Fabric API

This is a required library that the mod depends on.

**For 26.1.2:**
- Download: https://modrinth.com/mod/fabric-api/versions?g=26.1.2
- Look for the latest version that says **26.1.2** in the version list

**For 1.21.4:**
- Download: https://modrinth.com/mod/fabric-api/versions?g=1.21.4
- Look for the latest version that says **1.21.4** in the version list

Download the `.jar` file.

### 3. This Mod

Download the right `.jar` from this page (see the table above — both files are right here on GitHub).

---

## Installing the Mods

1. Open File Explorer and go to your Minecraft folder:
   - Press **Windows + R**, type `%appdata%\.minecraft`, press Enter
2. Open (or create) the **mods** folder inside it
3. Drop both `.jar` files into the **mods** folder:
   - `fabric-api-x.x.x+<version>.jar`
   - `qolmod-x.x.x.jar`
4. Open the Minecraft Launcher, select the Fabric profile for your version, and press Play

That's it! The mod will load automatically.

---

## Configuring the Mod (Optional)

If you also install **Mod Menu**, you'll get a nice in-game config screen where you can toggle each feature on or off. This is optional — all features are on by default.

- **For 26.1.2:** https://modrinth.com/mod/modmenu/versions?g=26.1.2
- **For 1.21.4:** https://modrinth.com/mod/modmenu/versions?g=1.21.4

---

## Troubleshooting

**Game crashes on launch:**
- Make sure you downloaded Fabric API for the **same version** as your Minecraft
- Make sure you're launching with the Fabric profile, not the default Java one

**Mod doesn't appear:**
- Double-check the `.jar` files are in `.minecraft/mods`, not in a subfolder

**Need help?** Message me!
