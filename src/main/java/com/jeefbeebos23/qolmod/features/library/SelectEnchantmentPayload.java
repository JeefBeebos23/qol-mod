package com.jeefbeebos23.qolmod.features.library;

import com.jeefbeebos23.qolmod.QolMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SelectEnchantmentPayload(Identifier enchantmentId) implements CustomPacketPayload {

    public static final Type<SelectEnchantmentPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "select_enchantment"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectEnchantmentPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.enchantmentId().toString()),
            buf -> new SelectEnchantmentPayload(Identifier.parse(buf.readUtf()))
        );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
