package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record MouseTweaksMoveOnePayload(List<Integer> slotIndices) implements CustomPacketPayload {
    public static final Type<MouseTweaksMoveOnePayload> ID =
        new Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "mouse_tweaks_move_one"));
    public static final StreamCodec<ByteBuf, MouseTweaksMoveOnePayload> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
            MouseTweaksMoveOnePayload::slotIndices,
            MouseTweaksMoveOnePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
