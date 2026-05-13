package com.jeefbeebos23.qolmod;

import com.jeefbeebos23.qolmod.features.AutoStackFeature;
import com.jeefbeebos23.qolmod.features.FurnaceXpFeature;
import com.jeefbeebos23.qolmod.features.ZoomFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class QolModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ZoomFeature.registerClient();
        AutoStackFeature.registerClient();
        FurnaceXpFeature.registerClient();
    }
}
