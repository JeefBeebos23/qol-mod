package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record KeyStatePayload(boolean veinMinerActive) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<KeyStatePayload> ID =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "key_state"));
    public static final StreamCodec<ByteBuf, KeyStatePayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.BOOL, KeyStatePayload::veinMinerActive, KeyStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
