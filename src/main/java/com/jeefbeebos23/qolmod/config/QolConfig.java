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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class QolConfig {
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("qolmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static QolConfig INSTANCE = new QolConfig();

    // Movement & Visual
    public boolean zoomEnabled = true;
    public boolean elytraDurabilityEnabled = true;

    // Mining
    public boolean veinMinerEnabled = true;
    public int veinMinerMaxBlocks = 64;
    public List<String> veinMinerBlocks = Arrays.asList(
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
    );

    // Inventory
    public boolean autoStackEnabled = true;
    public int autoStackRadius = 10;
    public boolean mouseTweaksEnabled = true;

    // Combat & Enchanting
    public boolean infinityBowEnabled = true;
    public boolean enchantLimitEnabled = true;

    // Crafting & Furnace
    public boolean slabRecipeEnabled = true;
    public boolean furnaceXpEnabled = true;

    public static QolConfig getInstance() { return INSTANCE; }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, QolConfig.class);
                if (INSTANCE == null) INSTANCE = new QolConfig();
            } catch (IOException e) {
                QolMod.LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new QolConfig();
            }
        }
    }

    public static void save() {
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
            .setTitle(Text.translatable("config.qolmod.title"))
            .setSavingRunnable(QolConfig::save);

        ConfigEntryBuilder eb = builder.entryBuilder();
        QolConfig cfg = INSTANCE;

        ConfigCategory movement = builder.getOrCreateCategory(
            Text.translatable("config.qolmod.category.movement"));
        movement.addEntry(eb.startBooleanToggle(
            Text.translatable("config.qolmod.zoom"), cfg.zoomEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.zoomEnabled = v).build());
        movement.addEntry(eb.startBooleanToggle(
            Text.translatable("config.qolmod.elytra"), cfg.elytraDurabilityEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.elytraDurabilityEnabled = v).build());

        ConfigCategory mining = builder.getOrCreateCategory(
            Text.translatable("config.qolmod.category.mining"));
        mining.addEntry(eb.startBooleanToggle(
            Text.translatable("config.qolmod.veinminer"), cfg.veinMinerEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.veinMinerEnabled = v).build());
        mining.addEntry(eb.startIntSlider(
            Text.translatable("config.qolmod.veinminer.max"), cfg.veinMinerMaxBlocks, 1, 256)
            .setDefaultValue(64).setSaveConsumer(v -> cfg.veinMinerMaxBlocks = v).build());
        mining.addEntry(eb.startStrList(
            Text.translatable("config.qolmod.veinminer.blocks"), cfg.veinMinerBlocks)
            .setDefaultValue(new QolConfig().veinMinerBlocks)
            .setSaveConsumer(v -> cfg.veinMinerBlocks = v).build());

        ConfigCategory inventory = builder.getOrCreateCategory(
            Text.translatable("config.qolmod.category.inventory"));
        inventory.addEntry(eb.startBooleanToggle(
            Text.translatable("config.qolmod.autostack"), cfg.autoStackEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.autoStackEnabled = v).build());
        inventory.addEntry(eb.startIntSlider(
            Text.translatable("config.qolmod.autostack.radius"), cfg.autoStackRadius, 1, 32)
            .setDefaultValue(10).setSaveConsumer(v -> cfg.autoStackRadius = v).build());
        inventory.addEntry(eb.startBooleanToggle(
            Text.translatable("config.qolmod.mousetweaks"), cfg.mouseTweaksEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.mouseTweaksEnabled = v).build());

        ConfigCategory combat = builder.getOrCreateCategory(
            Text.translatable("config.qolmod.category.combat"));
        combat.addEntry(eb.startBooleanToggle(
            Text.translatable("config.qolmod.infinitybow"), cfg.infinityBowEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.infinityBowEnabled = v).build());
        combat.addEntry(eb.startBooleanToggle(
            Text.translatable("config.qolmod.enchantlimit"), cfg.enchantLimitEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enchantLimitEnabled = v).build());

        ConfigCategory crafting = builder.getOrCreateCategory(
            Text.translatable("config.qolmod.category.crafting"));
        crafting.addEntry(eb.startBooleanToggle(
            Text.translatable("config.qolmod.slabrecipe"), cfg.slabRecipeEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.slabRecipeEnabled = v).build());
        crafting.addEntry(eb.startBooleanToggle(
            Text.translatable("config.qolmod.furnacexp"), cfg.furnaceXpEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.furnaceXpEnabled = v).build());

        return builder.build();
    }
}
