package net.tiffit.tconplanner.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiffit.tconplanner.TConPlanner;
import net.tiffit.tconplanner.data.PlannerSavedData;

@EventBusSubscriber(modid = TConPlanner.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            MinecraftServer server = sp.getServer();
            if (server == null || !sp.connection.hasChannel(BookmarkSyncPayload.TYPE)) {
                return;
            }
            PacketDistributor.sendToPlayer(sp, new BookmarkSyncPayload(PlannerSavedData.get(server).getFor(sp.getUUID())));
        }
    }
}
