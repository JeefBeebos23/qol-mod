package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ZoomFeature {
    private static KeyBinding zoomKey;
    private static final float ZOOM_FOV = 30.0f;
    private static float currentFov = -1f;  // -1 = needs initialization

    public static void registerClient() {
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.qolmod.zoom",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.category.qolmod"
        ));
    }

    public static boolean isZooming() {
        return QolConfig.getInstance().zoomEnabled
            && zoomKey != null
            && zoomKey.isPressed();
    }

    public static float getSmoothedFov(float baseFov) {
        if (currentFov < 0f) currentFov = baseFov;  // snap on first frame
        currentFov = currentFov + (ZOOM_FOV - currentFov) * 0.35f;
        return currentFov;
    }

    public static void resetFov() {
        currentFov = -1f;
    }
}
