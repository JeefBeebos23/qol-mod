package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.QolMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S packet: client requests the server to send back the current stored XP
 * for the furnace the player currently has open.
 */
public record FurnaceXpRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<FurnaceXpRequestPayload> ID =
        new CustomPayload.Id<>(Identifier.of(QolMod.MOD_ID, "furnace_xp_request"));
    public static final PacketCodec<ByteBuf, FurnaceXpRequestPayload> CODEC =
        PacketCodec.unit(new FurnaceXpRequestPayload());

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
