package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record QuickStackPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuickStackPayload> ID =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "quick_stack"));
    public static final StreamCodec<ByteBuf, QuickStackPayload> CODEC =
        StreamCodec.unit(new QuickStackPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
