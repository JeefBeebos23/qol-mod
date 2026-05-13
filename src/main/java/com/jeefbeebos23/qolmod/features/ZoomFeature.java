package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ZoomFeature {
    public static KeyBinding zoomKey;
    public static float zoomFov = 30.0f;

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
}
