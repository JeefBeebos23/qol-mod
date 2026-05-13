package com.jeefbeebos23.qolmod;

import com.jeefbeebos23.qolmod.features.AutoStackFeature;
import com.jeefbeebos23.qolmod.features.FurnaceXpFeature;
import com.jeefbeebos23.qolmod.features.KeyStatePayload;
import com.jeefbeebos23.qolmod.features.VeinMinerFeature;
import com.jeefbeebos23.qolmod.features.ZoomFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class QolModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ZoomFeature.registerClient();
        AutoStackFeature.registerClient();
        FurnaceXpFeature.registerClient();
        VeinMinerFeature.registerClient();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            boolean active = VeinMinerFeature.veinMinerKey != null
                && VeinMinerFeature.veinMinerKey.isPressed();
            ClientPlayNetworking.send(new KeyStatePayload(active));
        });
    }
}
