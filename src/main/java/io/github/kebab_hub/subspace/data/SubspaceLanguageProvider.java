package io.github.kebab_hub.subspace.data;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public final class SubspaceLanguageProvider extends LanguageProvider {
    public SubspaceLanguageProvider(DataGenerator generator) {
        super(generator, Subspace.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + Subspace.MOD_ID, "Subspace Engineering");
        add(ModRegistries.SUBSPACE_TEST_CASING.get(), "Subspace Test Casing");
        add(ModRegistries.PHASE_CRYSTAL.get(), "Phase Crystal");
        add(ModRegistries.STABILIZED_PHASE_CRYSTAL.get(), "Stabilized Phase Crystal");
        add(ModRegistries.RESONANCE_COIL.get(), "Resonance Coil");
        add(ModRegistries.PHASE_ALLOY_INGOT.get(), "Phase Alloy Ingot");
        add(ModRegistries.PHASE_ALLOY_PLATE.get(), "Phase Alloy Plate");
    }
}
