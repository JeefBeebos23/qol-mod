package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record KeyStatePayload(boolean veinMinerActive) implements CustomPayload {
    public static final CustomPayload.Id<KeyStatePayload> ID =
        new CustomPayload.Id<>(Identifier.of(QolMod.MOD_ID, "key_state"));
    public static final PacketCodec<ByteBuf, KeyStatePayload> CODEC =
        PacketCodec.tuple(PacketCodecs.BOOLEAN, KeyStatePayload::veinMinerActive, KeyStatePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
