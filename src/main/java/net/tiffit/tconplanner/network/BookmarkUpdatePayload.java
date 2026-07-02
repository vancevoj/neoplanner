package net.tiffit.tconplanner.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.tiffit.tconplanner.TConPlanner;

/** Client -> server: pushes the player's current bookmark blob after any change. */
public record BookmarkUpdatePayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<BookmarkUpdatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TConPlanner.MODID, "bookmark_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BookmarkUpdatePayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, BookmarkUpdatePayload::data, BookmarkUpdatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
