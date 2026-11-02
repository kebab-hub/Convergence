package io.github.kebab_hub.subspace.common.registry;

import io.github.kebab_hub.subspace.Subspace;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Common particle identities only; factories are registered exclusively on the client. */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Subspace.MOD_ID);
    public static final RegistryObject<SimpleParticleType> SUBSPACE_SPARK =
            PARTICLES.register("subspace_spark", () -> new SimpleParticleType(false));

    private ModParticles() {
    }

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
