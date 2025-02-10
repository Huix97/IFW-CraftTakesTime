package sn2.crafttakestime.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;
import sn2.crafttakestime.CraftTakesTime;
import sn2.crafttakestime.common.config.CraftConfig;


public record CraftConfigPayload(byte[] contents) implements CustomPacketPayload {

    public static final Type<CraftConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CraftTakesTime.MODID, "config"));

    public static final StreamCodec<FriendlyByteBuf, CraftConfigPayload> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.UNBOUNDED_BYTE_ARRAY, CraftConfigPayload::contents,
            CraftConfigPayload::new);

    @Override
    public @NotNull Type<CraftConfigPayload> type() {
        return TYPE;
    }

}
