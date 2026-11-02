package io.github.kebab_hub.subspace.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server/world settings; both directions and all faces of a pair share this allowance. */
public final class SubspaceConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue ITEMS_PER_TICK;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ITEMS_PER_TICK = builder.comment("Maximum items inserted plus extracted per direct interface pair per server tick.",
                "Shared across both endpoints and all faces. Simulation is free. Zero disables item transfers.")
                .defineInRange("interfaceItemsPerTick", 64, 0, 4096);
        SPEC = builder.build();
    }
    private SubspaceConfig() { }
}
