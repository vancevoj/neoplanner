package net.tiffit.tconplanner;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.tiffit.tconplanner.data.PlannerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(value = TConPlanner.MODID, dist = Dist.CLIENT)
public class TConPlanner {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "tconplanner";

    public static PlannerData DATA;

    public TConPlanner(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modBus.addListener(this::setupClient);
    }

    private void setupClient(final FMLClientSetupEvent event) {
        File gameDir = Minecraft.getInstance().gameDirectory;
        File folder = new File(gameDir, MODID);
        DATA = new PlannerData(folder);
    }


}
