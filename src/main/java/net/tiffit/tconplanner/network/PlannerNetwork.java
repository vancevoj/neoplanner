package net.tiffit.tconplanner.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.tiffit.tconplanner.TConPlanner;
import net.tiffit.tconplanner.data.PlannerSavedData;

@EventBusSubscriber(modid = TConPlanner.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PlannerNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        // optional() so the client still connects to vanilla / mod-less servers and vice versa
        PayloadRegistrar registrar = event.registrar("1").optional();

        // S2C: the client handler (which touches client-only BookmarkClient) is chosen ONLY on the client dist,
        // so BookmarkClient is never class-loaded on a dedicated server.
        registrar.playToClient(BookmarkSyncPayload.TYPE, BookmarkSyncPayload.CODEC,
                FMLEnvironment.dist == Dist.CLIENT
                        ? (payload, context) -> context.enqueueWork(() -> BookmarkClient.receive(payload.data()))
                        : (payload, context) -> {});

        // C2S: server stores the player's opaque bookmark blob
        registrar.playToServer(BookmarkUpdatePayload.TYPE, BookmarkUpdatePayload.CODEC, PlannerNetwork::handleUpdate);
    }

    private static void handleUpdate(BookmarkUpdatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                MinecraftServer server = sp.getServer();
                if (server != null) {
                    PlannerSavedData.get(server).setFor(sp.getUUID(), payload.data());
                }
            }
        });
    }
}
