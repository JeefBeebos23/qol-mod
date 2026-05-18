package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record InventorySortPayload(List<String> hotbarLayout) implements CustomPacketPayload {

    public InventorySortPayload {
        if (hotbarLayout.size() != 10)
            throw new IllegalArgumentException("hotbarLayout must have exactly 10 entries, got " + hotbarLayout.size());
        hotbarLayout = Collections.unmodifiableList(hotbarLayout);
    }

    public static final Type<InventorySortPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(QolMod.MOD_ID, "sort_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InventorySortPayload> CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                for (String s : payload.hotbarLayout()) {
                    buf.writeBoolean(s != null);
                    if (s != null) buf.writeUtf(s);
                }
            },
            buf -> {
                List<String> layout = new ArrayList<>(9);
                for (int i = 0; i < 10; i++) layout.add(buf.readBoolean() ? buf.readUtf(256) : null);
                return new InventorySortPayload(layout);
            }
        );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
