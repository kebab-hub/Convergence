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
        add(ModRegistries.SUBSPACE_DETECTOR.get(), "Subspace Detector");
        add("message.subspace.detector.reading", "Subspace activity: %s/100 - %s");
        add("message.subspace.detector.quiet", "Quiet");
        add("message.subspace.detector.steady", "Steady");
        add("message.subspace.detector.elevated", "Elevated");
        add(ModRegistries.PHASE_LINKER.get(), "Phase Linker");
        add("tooltip.subspace.phase_linker.select", "Right-click a block: select its face");
        add("tooltip.subspace.phase_linker.status", "Right-click air: check saved endpoints");
        add("tooltip.subspace.phase_linker.clear", "Sneak-right-click: clear endpoints");
        add("tooltip.subspace.phase_linker.next_a", "Next: select endpoint A");
        add("tooltip.subspace.phase_linker.next_b", "Next: select endpoint B");
        add("tooltip.subspace.phase_linker.restart", "Pair recorded. Next selection starts a new A.");
        add("tooltip.subspace.phase_linker.empty", "Endpoint %s: not selected");
        add("tooltip.subspace.phase_linker.address", "Endpoint %s: %s [%s, %s, %s] - %s");
        add("message.subspace.phase_linker.selected_a", "A recorded at [%s, %s, %s] - %s. Select B.");
        add("message.subspace.phase_linker.selected_b", "Endpoints A and B recorded. A: %s. Check details in the tooltip.");
        add("message.subspace.phase_linker.cleared", "Endpoints cleared. Select endpoint A.");
        add("message.subspace.phase_linker.malformed", "Saved endpoints are invalid. Sneak-right-click to clear.");
        add("message.subspace.phase_linker.duplicate", "Choose a different position or face for endpoint B.");
        add("message.subspace.phase_linker.forbidden", "You cannot select an endpoint here.");
        add("message.subspace.phase_linker.rejected", "Endpoint not selected: %s.");
        add("message.subspace.phase_linker.status", "%s | %s");
        add("status.subspace.phase_linker.available", "Available");
        add("status.subspace.phase_linker.dimension_unavailable", "Dimension unavailable");
        add("status.subspace.phase_linker.out_of_bounds", "Outside build limits or world border");
        add("status.subspace.phase_linker.unloaded", "Chunk unloaded (address retained)");
        add("status.subspace.phase_linker.block_missing", "Block missing (address retained)");
        add("face.subspace.phase_linker.down", "Down");
        add("face.subspace.phase_linker.up", "Up");
        add("face.subspace.phase_linker.north", "North");
        add("face.subspace.phase_linker.south", "South");
        add("face.subspace.phase_linker.west", "West");
        add("face.subspace.phase_linker.east", "East");
    }
}
