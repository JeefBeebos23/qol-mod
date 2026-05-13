package com.jeefbeebos23.qolmod;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.FurnaceXpFeature;
import com.jeefbeebos23.qolmod.features.VeinMinerFeature;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QolMod implements ModInitializer {
    public static final String MOD_ID = "qolmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        QolConfig.load();
        VeinMinerFeature.register();
        FurnaceXpFeature.register();
    }
}
