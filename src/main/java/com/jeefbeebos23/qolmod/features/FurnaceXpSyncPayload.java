package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FurnaceXpSyncPayload(int xp) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FurnaceXpSyncPayload> ID =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "furnace_xp_sync"));
    public static final StreamCodec<ByteBuf, FurnaceXpSyncPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.INT, FurnaceXpSyncPayload::xp, FurnaceXpSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
