package io.github.kebab_hub.subspace.data;

import net.minecraftforge.data.event.GatherDataEvent;

/**
 * The mod-bus entry point for data generation. Add providers here alongside the
 * first content that needs them, using includeClient/includeServer to respect
 * the requested output. Providers write to src/generated/resources; their
 * output is committed and packaged with the hand-authored resources.
 */
public final class SubspaceData {
    private SubspaceData() {
    }

    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeClient(),
                new SubspaceLanguageProvider(event.getGenerator()));
        event.getGenerator().addProvider(event.includeClient(),
                new SubspaceItemModelProvider(event.getGenerator(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeServer(),
                new SubspaceRecipeProvider(event.getGenerator()));
        event.getGenerator().addProvider(event.includeServer(),
                new SubspaceItemTagProvider(event.getGenerator(), event.getExistingFileHelper()));
    }
}
