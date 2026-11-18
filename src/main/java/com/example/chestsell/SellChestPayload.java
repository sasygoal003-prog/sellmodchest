package com.example.chestsell;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SellChestPayload() implements CustomPayload {
    public static final CustomPayload.Id<SellChestPayload> ID =
        new CustomPayload.Id<>(Identifier.of("chestsell", "sell_all"));
    public static final PacketCodec<RegistryByteBuf, SellChestPayload> CODEC =
        PacketCodec.unit(new SellChestPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
