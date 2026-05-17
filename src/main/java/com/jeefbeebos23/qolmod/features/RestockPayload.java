package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RestockPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RestockPayload> ID =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "restock"));
    public static final StreamCodec<ByteBuf, RestockPayload> CODEC =
        StreamCodec.unit(new RestockPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
