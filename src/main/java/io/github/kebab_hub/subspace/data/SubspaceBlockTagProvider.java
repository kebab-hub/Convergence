package io.github.kebab_hub.subspace.data;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class SubspaceBlockTagProvider extends BlockTagsProvider {
    public SubspaceBlockTagProvider(DataGenerator generator, ExistingFileHelper existingFiles) {
        super(generator, Subspace.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModRegistries.SUBSPACE_TEST_CASING.get(),
                ModRegistries.SUBSPACE_DETECTOR.get(), ModRegistries.SUBSPACE_ANCHOR.get());
    }
}
