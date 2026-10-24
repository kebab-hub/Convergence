package io.github.kebab_hub.convergence;

import com.mojang.logging.LogUtils;
import io.github.kebab_hub.convergence.common.registry.ModRegistries;
import io.github.kebab_hub.convergence.data.ConvergenceData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Convergence.MOD_ID)
public final class Convergence {
    public static final String MOD_ID = "convergence";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Convergence(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ModRegistries.register(modBus);
        modBus.addListener(ConvergenceData::gatherData);
        LOGGER.info("Initializing Convergence");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
