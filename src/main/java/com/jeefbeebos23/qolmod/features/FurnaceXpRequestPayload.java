package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FurnaceXpRequestPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FurnaceXpRequestPayload> ID =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "furnace_xp_request"));
    public static final StreamCodec<ByteBuf, FurnaceXpRequestPayload> CODEC =
        StreamCodec.unit(new FurnaceXpRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
