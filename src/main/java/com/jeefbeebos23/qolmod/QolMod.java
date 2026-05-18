package com.jeefbeebos23.qolmod;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.AutoStackFeature;
import com.jeefbeebos23.qolmod.features.FurnaceXpFeature;
import com.jeefbeebos23.qolmod.features.KeyStatePayload;
import com.jeefbeebos23.qolmod.features.MouseTweaksFeature;
import com.jeefbeebos23.qolmod.features.TreeReplantFeature;
import com.jeefbeebos23.qolmod.features.VeinMinerFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QolMod implements ModInitializer {
    public static final String MOD_ID = "qolmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        QolConfig.load();
        VeinMinerFeature.register();
        TreeReplantFeature.register();
        AutoStackFeature.register();
        MouseTweaksFeature.register();
        FurnaceXpFeature.register();

        PayloadTypeRegistry.serverboundPlay().register(KeyStatePayload.ID, KeyStatePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KeyStatePayload.ID, (payload, context) ->
            context.server().execute(() ->
                VeinMinerFeature.setActive(context.player().getUUID(), payload.veinMinerActive())
            )
        );
    }
}
