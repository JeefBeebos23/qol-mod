package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ZoomFeature {
    private static KeyMapping zoomKey;
    private static final float ZOOM_FOV = 30.0f;
    private static float currentFov = -1f;

    public static void registerClient(KeyMapping.Category category) {
        zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.qolmod.zoom",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            category
        ));
    }

    public static boolean isZooming() {
        return QolConfig.getInstance().zoomEnabled
            && zoomKey != null
            && zoomKey.isDown();
    }

    public static float getSmoothedFov(float baseFov) {
        if (currentFov < 0f) currentFov = baseFov;
        currentFov = currentFov + (ZOOM_FOV - currentFov) * 0.35f;
        return currentFov;
    }

    public static void resetFov() {
        currentFov = -1f;
    }
}
