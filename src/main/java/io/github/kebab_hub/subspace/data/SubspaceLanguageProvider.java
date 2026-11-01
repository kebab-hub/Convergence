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
        add("tooltip.subspace.phase_linker.clear", "Sneak-right-click air: clear endpoints");
        add("tooltip.subspace.phase_linker.interface", "Sneak-use on Interface: apply endpoint A");
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
        add(ModRegistries.SUBSPACE_ANCHOR.get(), "Subspace Anchor");
        add("screen.subspace.anchor.address", "Network address");
        add("screen.subspace.anchor.status", "Status: %s");
        add("screen.subspace.anchor.links", "Linked endpoints: %s");
        add("screen.subspace.anchor.active", "Active / stable");
        add("screen.subspace.anchor.inactive", "Inactive");
        add("screen.subspace.anchor.syncing", "Synchronizing...");
        add("screen.subspace.anchor.activate", "Activate");
        add("screen.subspace.anchor.deactivate", "Deactivate");
        add(ModRegistries.SUBSPACE_INTERFACE.get(), "Subspace Interface");
        add("message.subspace.interface.target", "Target: %s [%s, %s, %s] - %s");
        add("message.subspace.interface.state", "Interface: %s");
        add("message.subspace.interface.channel", "%s: %s");
        add("message.subspace.interface.available", "Detected on target side");
        add("message.subspace.interface.unavailable", "Unavailable");
        add("message.subspace.interface.help", "Linker: select A, then sneak-use on Interface. Empty-hand sneak-use: clear. No transport yet.");
        add("message.subspace.interface.cleared", "Interface target cleared.");
        add("message.subspace.interface.forbidden", "You cannot configure this interface here.");
        add("message.subspace.interface.select_a", "Select a valid endpoint A with the Phase Linker first.");
        add("message.subspace.interface.invalid_link", "Self-links and links to another Interface are not supported.");
        add("channel.subspace.items", "Item handler");
        add("channel.subspace.fluids", "Fluid handler");
        add("channel.subspace.energy", "Forge Energy");
        add("state.subspace.interface.unbound", "No target configured");
        add("state.subspace.interface.invalid_data", "Invalid saved target; clear or reconfigure");
        add("state.subspace.interface.self_link", "Self-link rejected");
        add("state.subspace.interface.dimension_unavailable", "Target dimension unavailable");
        add("state.subspace.interface.out_of_bounds", "Target outside build limits or world border");
        add("state.subspace.interface.unloaded", "Target chunk unloaded (address retained)");
        add("state.subspace.interface.block_missing", "Target block missing (address retained)");
        add("state.subspace.interface.no_block_entity", "Target has no available block entity");
        add("state.subspace.interface.chain_unsupported", "Interface chains are not supported");
        add("state.subspace.interface.unsupported", "No supported capabilities on target side");
        add("state.subspace.interface.capability_error", "Target capability query failed");
        add("state.subspace.interface.ready", "Target available (transport not implemented)");
    }
}
