package com.jeefbeebos23.qolmod;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.AutoRefillFeature;
import com.jeefbeebos23.qolmod.features.AutoStackFeature;
import com.jeefbeebos23.qolmod.features.ChestSortPayload;
import com.jeefbeebos23.qolmod.features.CropReplantFeature;
import com.jeefbeebos23.qolmod.features.FortuneBonemealFeature;
import com.jeefbeebos23.qolmod.features.FurnaceXpFeature;
import com.jeefbeebos23.qolmod.features.MagicMirrorPayload;
import com.jeefbeebos23.qolmod.features.InventorySortPayload;
import com.jeefbeebos23.qolmod.features.InventorySorter;
import com.jeefbeebos23.qolmod.features.KeyStatePayload;
import com.jeefbeebos23.qolmod.features.LibraryVillagerFeature;
import com.jeefbeebos23.qolmod.features.MouseTweaksFeature;
import com.jeefbeebos23.qolmod.features.RestockFeature;
import com.jeefbeebos23.qolmod.features.RestockPayload;
import com.jeefbeebos23.qolmod.features.TreeReplantFeature;
import com.jeefbeebos23.qolmod.features.VeinMinerFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
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
        CropReplantFeature.register();
        AutoStackFeature.register();
        AutoRefillFeature.register();
        MouseTweaksFeature.register();
        FurnaceXpFeature.register();
        LibraryVillagerFeature.register();
        FortuneBonemealFeature.register();

        PayloadTypeRegistry.serverboundPlay().register(KeyStatePayload.ID, KeyStatePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KeyStatePayload.ID, (payload, context) ->
            context.server().execute(() ->
                VeinMinerFeature.setActive(context.player().getUUID(), payload.veinMinerActive())
            )
        );

        PayloadTypeRegistry.serverboundPlay().register(RestockPayload.TYPE, RestockPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RestockPayload.TYPE, (payload, context) ->
            context.server().execute(() -> RestockFeature.restock(context.player()))
        );

        PayloadTypeRegistry.serverboundPlay().register(InventorySortPayload.TYPE, InventorySortPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(InventorySortPayload.TYPE, (payload, context) ->
            context.server().execute(() ->
                InventorySorter.sort(context.player(), payload.hotbarLayout()))
        );

        PayloadTypeRegistry.serverboundPlay().register(ChestSortPayload.TYPE, ChestSortPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ChestSortPayload.TYPE, (payload, context) ->
            context.server().execute(() -> InventorySorter.sortChest(context.player()))
        );

        PayloadTypeRegistry.serverboundPlay().register(MagicMirrorPayload.TYPE, MagicMirrorPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MagicMirrorPayload.TYPE, (payload, context) ->
            context.server().execute(() -> teleportToSpawn(context.player()))
        );
    }

    private static void teleportToSpawn(ServerPlayer player) {
        TeleportTransition tt = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
        player.teleport(tt);
    }
}
