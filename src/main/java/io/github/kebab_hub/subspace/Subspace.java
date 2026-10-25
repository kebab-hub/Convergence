package io.github.kebab_hub.subspace;

import com.mojang.logging.LogUtils;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import io.github.kebab_hub.subspace.data.SubspaceData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Subspace.MOD_ID)
public final class Subspace {
    public static final String MOD_ID = "subspace";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Subspace(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ModRegistries.register(modBus);
        modBus.addListener(SubspaceData::gatherData);
        LOGGER.info("Initializing Subspace Engineering");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
