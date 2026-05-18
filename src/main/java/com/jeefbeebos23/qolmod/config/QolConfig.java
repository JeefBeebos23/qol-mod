package com.jeefbeebos23.qolmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeefbeebos23.qolmod.QolMod;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QolConfig {
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("qolmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile QolConfig INSTANCE = new QolConfig();

    // Movement & Visual
    public boolean zoomEnabled = true;
    public boolean elytraDurabilityEnabled = true;

    // Mining
    public boolean veinMinerEnabled = true;
    public boolean treeReplantEnabled = true;
    public int veinMinerMaxBlocks = 64;
    public List<String> veinMinerBlocks = new ArrayList<>(Arrays.asList(
        "minecraft:coal_ore", "minecraft:deepslate_coal_ore",
        "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
        "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
        "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
        "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
        "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
        "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
        "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
        "minecraft:nether_gold_ore", "minecraft:nether_quartz_ore",
        "minecraft:ancient_debris",
        "minecraft:oak_log", "minecraft:spruce_log", "minecraft:birch_log",
        "minecraft:jungle_log", "minecraft:acacia_log", "minecraft:dark_oak_log",
        "minecraft:mangrove_log", "minecraft:cherry_log", "minecraft:bamboo_block",
        "minecraft:crimson_stem", "minecraft:warped_stem"
    ));

    // Inventory
    public boolean autoStackEnabled = true;
    public int autoStackRadius = 10;
    public boolean mouseTweaksEnabled = true;

    // Combat & Enchanting
    public boolean infinityBowEnabled = true;
    public boolean enchantLimitEnabled = true;
    public boolean mendingInfinityEnabled = true;

    // Crafting & Furnace
    public boolean slabRecipeEnabled = true;
    public boolean furnaceXpEnabled = true;

    public static QolConfig getInstance() { return INSTANCE; }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, QolConfig.class);
                if (INSTANCE == null) INSTANCE = new QolConfig();
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                QolMod.LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new QolConfig();
            }
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
        } catch (IOException e) {
            QolMod.LOGGER.error("Failed to create config directory", e);
            return;
        }
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            QolMod.LOGGER.error("Failed to save config", e);
        }
    }

    @Environment(EnvType.CLIENT)
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("config.qolmod.title"))
            .setSavingRunnable(QolConfig::save);

        ConfigEntryBuilder eb = builder.entryBuilder();

        ConfigCategory movement = builder.getOrCreateCategory(
            Component.translatable("config.qolmod.category.movement"));
        movement.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.zoom"), QolConfig.getInstance().zoomEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().zoomEnabled = v).build());
        movement.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.elytra"), QolConfig.getInstance().elytraDurabilityEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().elytraDurabilityEnabled = v).build());

        ConfigCategory mining = builder.getOrCreateCategory(
            Component.translatable("config.qolmod.category.mining"));
        mining.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.veinminer"), QolConfig.getInstance().veinMinerEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().veinMinerEnabled = v).build());
        mining.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.treereplant"), QolConfig.getInstance().treeReplantEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().treeReplantEnabled = v).build());
        mining.addEntry(eb.startIntSlider(
            Component.translatable("config.qolmod.veinminer.max"), QolConfig.getInstance().veinMinerMaxBlocks, 1, 256)
            .setDefaultValue(64).setSaveConsumer(v -> QolConfig.getInstance().veinMinerMaxBlocks = v).build());
        mining.addEntry(eb.startStrList(
            Component.translatable("config.qolmod.veinminer.blocks"), QolConfig.getInstance().veinMinerBlocks)
            .setDefaultValue(new ArrayList<>(new QolConfig().veinMinerBlocks))
            .setSaveConsumer(v -> QolConfig.getInstance().veinMinerBlocks = v).build());

        ConfigCategory inventory = builder.getOrCreateCategory(
            Component.translatable("config.qolmod.category.inventory"));
        inventory.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.autostack"), QolConfig.getInstance().autoStackEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().autoStackEnabled = v).build());
        inventory.addEntry(eb.startIntSlider(
            Component.translatable("config.qolmod.autostack.radius"), QolConfig.getInstance().autoStackRadius, 1, 32)
            .setDefaultValue(10).setSaveConsumer(v -> QolConfig.getInstance().autoStackRadius = v).build());
        inventory.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.mousetweaks"), QolConfig.getInstance().mouseTweaksEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().mouseTweaksEnabled = v).build());

        ConfigCategory combat = builder.getOrCreateCategory(
            Component.translatable("config.qolmod.category.combat"));
        combat.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.infinitybow"), QolConfig.getInstance().infinityBowEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().infinityBowEnabled = v).build());
        combat.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.enchantlimit"), QolConfig.getInstance().enchantLimitEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().enchantLimitEnabled = v).build());
        combat.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.mendinginfinity"), QolConfig.getInstance().mendingInfinityEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().mendingInfinityEnabled = v).build());

        ConfigCategory crafting = builder.getOrCreateCategory(
            Component.translatable("config.qolmod.category.crafting"));
        crafting.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.slabrecipe"), QolConfig.getInstance().slabRecipeEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().slabRecipeEnabled = v).build());
        crafting.addEntry(eb.startBooleanToggle(
            Component.translatable("config.qolmod.furnacexp"), QolConfig.getInstance().furnaceXpEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> QolConfig.getInstance().furnaceXpEnabled = v).build());

        return builder.build();
    }
}
