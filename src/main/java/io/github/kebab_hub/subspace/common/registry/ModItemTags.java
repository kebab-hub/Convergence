package io.github.kebab_hub.subspace.common.registry;

import io.github.kebab_hub.subspace.Subspace;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Material interchange tags are separate from assembled component identities. */
public final class ModItemTags {
    public static final TagKey<Item> PHASE_CRYSTALS = forge("gems/phase_crystal");
    public static final TagKey<Item> PHASE_ALLOY_INGOTS = forge("ingots/phase_alloy");
    public static final TagKey<Item> PHASE_ALLOY_PLATES = forge("plates/phase_alloy");
    public static final TagKey<Item> PLATES = forge("plates");
    public static final TagKey<Item> STABILIZED_PHASE_CRYSTALS =
            ItemTags.create(Subspace.id("stabilized_phase_crystals"));
    public static final TagKey<Item> RESONANCE_COILS = ItemTags.create(Subspace.id("resonance_coils"));
    public static final TagKey<Item> COMPONENTS = ItemTags.create(Subspace.id("components"));

    private ModItemTags() {
    }

    private static TagKey<Item> forge(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", path));
    }
}
