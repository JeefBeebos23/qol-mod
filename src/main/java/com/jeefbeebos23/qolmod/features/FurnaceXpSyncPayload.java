package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * S2C packet: server sends the current stored XP amount to the client.
 */
public record FurnaceXpSyncPayload(int xp) implements CustomPayload {
    public static final CustomPayload.Id<FurnaceXpSyncPayload> ID =
        new CustomPayload.Id<>(Identifier.of(QolMod.MOD_ID, "furnace_xp_sync"));
    public static final PacketCodec<ByteBuf, FurnaceXpSyncPayload> CODEC =
        PacketCodec.tuple(PacketCodecs.INTEGER, FurnaceXpSyncPayload::xp, FurnaceXpSyncPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
