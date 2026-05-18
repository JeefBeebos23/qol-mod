package com.jeefbeebos23.qolmod;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.ChestSortPayload;
import com.jeefbeebos23.qolmod.features.FurnaceXpFeature;
import com.jeefbeebos23.qolmod.features.KeyStatePayload;
import com.jeefbeebos23.qolmod.features.ZoomFeature;
import com.jeefbeebos23.qolmod.mixin.ScreenInvoker;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ChestMenu;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class QolModClient implements ClientModInitializer {

    static final KeyMapping.Category QOL_CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "key.category"));

    private static KeyMapping veinMinerKey;
    private static KeyMapping configKey;
    private static boolean lastVeinMinerActive = false;

    @Override
    public void onInitializeClient() {
        ZoomFeature.registerClient(QOL_CATEGORY);
        FurnaceXpFeature.registerClient();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> acs)) return;
            if (!(acs.getMenu() instanceof ChestMenu chestMenu)) return;
            int imageWidth = 176;
            int imageHeight = 114 + chestMenu.getRowCount() * 18;
            int leftPos = (scaledWidth - imageWidth) / 2;
            int topPos = (scaledHeight - imageHeight) / 2;
            ((ScreenInvoker) screen).invokeAddRenderableWidget(
                Button.builder(Component.translatable("qolmod.sort"),
                    btn -> ClientPlayNetworking.send(new ChestSortPayload()))
                    .pos(leftPos + imageWidth + 4, topPos + 4)
                    .size(70, 20)
                    .build()
            );
        });

        veinMinerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.qolmod.veinminer",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            QOL_CATEGORY
        ));

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.qolmod.config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            QOL_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            boolean active = veinMinerKey.isDown();
            if (active != lastVeinMinerActive) {
                lastVeinMinerActive = active;
                ClientPlayNetworking.send(new KeyStatePayload(active));
            }
            if (configKey.consumeClick()) {
                client.setScreen(QolConfig.createConfigScreen(client.screen));
            }
        });
    }
}
