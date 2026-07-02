package net.tiffit.tconplanner.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.tiffit.tconplanner.TConPlanner;

/** Server -> client: delivers the player's stored bookmark blob on login. */
public record BookmarkSyncPayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<BookmarkSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TConPlanner.MODID, "bookmark_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BookmarkSyncPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, BookmarkSyncPayload::data, BookmarkSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
