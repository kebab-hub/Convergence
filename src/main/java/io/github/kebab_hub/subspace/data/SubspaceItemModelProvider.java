package io.github.kebab_hub.subspace.data;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class SubspaceItemModelProvider extends ItemModelProvider {
    public SubspaceItemModelProvider(DataGenerator generator, ExistingFileHelper existingFiles) {
        super(generator, Subspace.MOD_ID, existingFiles);
    }

    @Override
    protected void registerModels() {
        basicItem(ModRegistries.PHASE_CRYSTAL.get());
        basicItem(ModRegistries.STABILIZED_PHASE_CRYSTAL.get());
        basicItem(ModRegistries.RESONANCE_COIL.get());
        basicItem(ModRegistries.PHASE_ALLOY_INGOT.get());
        basicItem(ModRegistries.PHASE_ALLOY_PLATE.get());
    }
}
