package net.tiffit.tconplanner.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side per-player bookmark storage. Keeps an opaque NBT blob (the same root tag the client writes to
 * bookmark.dat) keyed by player UUID, so a player's saved planner designs live on the server and follow them
 * across clients. Deliberately imports ONLY vanilla NBT/server types (no client or TConstruct classes) so it is
 * safe to load on a dedicated server.
 */
public class PlannerSavedData extends SavedData {
    private static final String NAME = "neoplanner_bookmarks";

    private final Map<UUID, CompoundTag> byPlayer = new HashMap<>();

    public static PlannerSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PlannerSavedData::new, PlannerSavedData::load, null), NAME);
    }

    /** Returns a defensive copy of the stored blob for the player (empty tag if none stored yet). */
    public CompoundTag getFor(UUID id) {
        CompoundTag tag = byPlayer.get(id);
        return tag != null ? tag.copy() : new CompoundTag();
    }

    public void setFor(UUID id, CompoundTag data) {
        byPlayer.put(id, data.copy());
        setDirty();
    }

    public static PlannerSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        PlannerSavedData out = new PlannerSavedData();
        CompoundTag players = tag.getCompound("players");
        for (String key : players.getAllKeys()) {
            try {
                out.byPlayer.put(UUID.fromString(key), players.getCompound(key));
            } catch (IllegalArgumentException ignored) {
                // skip malformed UUID keys rather than fail the whole load
            }
        }
        return out;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag players = new CompoundTag();
        byPlayer.forEach((id, data) -> players.put(id.toString(), data));
        tag.put("players", players);
        return tag;
    }
}
