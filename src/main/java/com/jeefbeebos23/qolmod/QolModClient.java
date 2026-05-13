package com.jeefbeebos23.qolmod;

import com.jeefbeebos23.qolmod.features.AutoStackFeature;
import com.jeefbeebos23.qolmod.features.FurnaceXpFeature;
import com.jeefbeebos23.qolmod.features.KeyStatePayload;
import com.jeefbeebos23.qolmod.features.ZoomFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class QolModClient implements ClientModInitializer {

    private static KeyBinding veinMinerKey;
    private static boolean lastVeinMinerActive = false;

    @Override
    public void onInitializeClient() {
        ZoomFeature.registerClient();
        AutoStackFeature.registerClient();
        FurnaceXpFeature.registerClient();

        veinMinerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.qolmod.veinminer",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            "key.category.qolmod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            boolean active = veinMinerKey.isPressed();
            if (active != lastVeinMinerActive) {
                lastVeinMinerActive = active;
                ClientPlayNetworking.send(new KeyStatePayload(active));
            }
        });
    }
}
