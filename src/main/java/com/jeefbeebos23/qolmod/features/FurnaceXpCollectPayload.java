package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S packet: client requests the server to collect (spawn as orbs) the stored
 * furnace XP for the furnace the player currently has open.
 */
public record FurnaceXpCollectPayload() implements CustomPayload {
    public static final CustomPayload.Id<FurnaceXpCollectPayload> ID =
        new CustomPayload.Id<>(Identifier.of(QolMod.MOD_ID, "furnace_xp_collect"));
    public static final PacketCodec<ByteBuf, FurnaceXpCollectPayload> CODEC =
        PacketCodec.unit(new FurnaceXpCollectPayload());

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
