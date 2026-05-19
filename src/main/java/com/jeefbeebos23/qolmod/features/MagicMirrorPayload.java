package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MagicMirrorPayload() implements CustomPacketPayload {

    public static final Type<MagicMirrorPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "magic_mirror"));

    public static final StreamCodec<ByteBuf, MagicMirrorPayload> CODEC =
        StreamCodec.unit(new MagicMirrorPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
