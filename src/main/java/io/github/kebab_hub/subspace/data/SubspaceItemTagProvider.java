package io.github.kebab_hub.subspace.data;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.registry.ModItemTags;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.ForgeRegistries;

/** Item-only tags; no block-tag copying or empty block provider is needed. */
public final class SubspaceItemTagProvider extends ForgeRegistryTagsProvider<Item> {
    public SubspaceItemTagProvider(DataGenerator generator, ExistingFileHelper existingFiles) {
        super(generator, ForgeRegistries.ITEMS, Subspace.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags() {
        tag(ModItemTags.PHASE_CRYSTALS).add(ModRegistries.PHASE_CRYSTAL.get());
        tag(ModItemTags.STABILIZED_PHASE_CRYSTALS).add(ModRegistries.STABILIZED_PHASE_CRYSTAL.get());
        tag(ModItemTags.RESONANCE_COILS).add(ModRegistries.RESONANCE_COIL.get());
        tag(ModItemTags.PHASE_ALLOY_INGOTS).add(ModRegistries.PHASE_ALLOY_INGOT.get());
        tag(ModItemTags.PHASE_ALLOY_PLATES).add(ModRegistries.PHASE_ALLOY_PLATE.get());
        tag(Tags.Items.GEMS).addTag(ModItemTags.PHASE_CRYSTALS);
        tag(Tags.Items.INGOTS).addTag(ModItemTags.PHASE_ALLOY_INGOTS);
        tag(ModItemTags.PLATES).addTag(ModItemTags.PHASE_ALLOY_PLATES);
        tag(ModItemTags.COMPONENTS).addTag(ModItemTags.PHASE_CRYSTALS)
                .addTag(ModItemTags.STABILIZED_PHASE_CRYSTALS).addTag(ModItemTags.RESONANCE_COILS)
                .addTag(ModItemTags.PHASE_ALLOY_INGOTS).addTag(ModItemTags.PHASE_ALLOY_PLATES);
    }

    @Override
    public String getName() {
        return "Subspace Engineering item tags";
    }
}
