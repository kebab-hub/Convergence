package io.github.kebab_hub.subspace.client;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.client.particle.SubspaceSparkParticle;
import io.github.kebab_hub.subspace.common.registry.ModParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Subspace.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientParticles {
    private ClientParticles() {
    }

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.register(ModParticles.SUBSPACE_SPARK.get(), SubspaceSparkParticle.Provider::new);
    }
}
