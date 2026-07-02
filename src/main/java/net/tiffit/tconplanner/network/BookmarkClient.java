package net.tiffit.tconplanner.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiffit.tconplanner.TConPlanner;

import java.io.IOException;

/**
 * Client-side glue for server bookmark sync. Only ever referenced/loaded on the client (the payload handler that
 * touches this class is selected behind a Dist check in {@link PlannerNetwork}).
 */
public class BookmarkClient {

    /** True if the current server connection speaks our bookmark channel (i.e. the server has Neo Planner). */
    public static boolean serverHasChannel() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getConnection() != null && mc.getConnection().hasChannel(BookmarkUpdatePayload.TYPE);
    }

    /** Push the client's current bookmarks up to the server, if it has the mod. No-op otherwise (local file only). */
    public static void push() {
        if (TConPlanner.DATA == null || !serverHasChannel()) {
            return;
        }
        PacketDistributor.sendToServer(new BookmarkUpdatePayload(TConPlanner.DATA.toNBT()));
    }

    /** Handle a bookmark blob pushed from the server on login. */
    public static void receive(CompoundTag data) {
        if (TConPlanner.DATA == null) {
            return;
        }
        boolean serverHasData = !data.getList("list", Tag.TAG_COMPOUND).isEmpty() || data.contains("starred");
        if (serverHasData) {
            // server is the source of truth: adopt its bookmarks and mirror them to the local file
            TConPlanner.DATA.loadFromNBT(data);
            try {
                TConPlanner.DATA.save();
            } catch (IOException ignored) {
                // local mirror is best-effort; server copy is authoritative
            }
        } else if (!TConPlanner.DATA.isEmpty()) {
            // server has nothing stored yet: seed it from the player's local bookmarks
            push();
        }
    }
}
