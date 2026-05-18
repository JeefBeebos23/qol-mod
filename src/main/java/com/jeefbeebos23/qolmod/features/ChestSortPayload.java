package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ChestSortPayload() implements CustomPacketPayload {

    public static final Type<ChestSortPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "sort_chest"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChestSortPayload> CODEC =
        StreamCodec.unit(new ChestSortPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
