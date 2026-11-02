# First Subspace Components

These five ordinary stackable items are implemented. They are crafting ingredients, with no machine,
energy, network, portal, or pocket-space behavior. All recipes work with vanilla resources and these
components alone. Every output is available in the Subspace Engineering creative tab.

## Crafting

| Output | Ingredients | Layout |
| --- | --- | --- |
| 1 Phase Crystal | 2 amethyst shards + 1 redstone dust + 1 quartz | Shapeless |
| 1 Stabilized Phase Crystal | 1 Phase Crystal + 2 iron ingots + 2 copper ingots | Iron above/below the crystal; copper left/right |
| 1 Resonance Coil | 1 Stabilized Phase Crystal + 4 copper ingots | Copper above/below/left/right of the crystal |
| 3 Phase Alloy Ingots | 2 iron ingots + 1 copper ingot + 1 Phase Crystal | Shapeless |
| 1 Phase Alloy Plate | 2 Phase Alloy Ingots | Vertical pair |

The framed crystal and coil use a crafting table. The other recipes fit in the 2×2 inventory grid.
The alloy recipe consumes its crystal additive; plate crafting consumes both ingots and has no reverse
recipe. No hammer, machine, or extra material is required. Recipe-book unlocks follow possession of
amethyst, Phase Crystal, Stabilized Phase Crystal, or Phase Alloy Ingot as appropriate.

## IDs and tags

| Item ID (under `subspace:`) | Specific ingredient tag |
| --- | --- |
| `phase_crystal` | `forge:gems/phase_crystal` |
| `stabilized_phase_crystal` | `subspace:stabilized_phase_crystals` |
| `resonance_coil` | `subspace:resonance_coils` |
| `phase_alloy_ingot` | `forge:ingots/phase_alloy` |
| `phase_alloy_plate` | `forge:plates/phase_alloy` |

All five belong to `subspace:components`. The loose crystal, ingot, and plate also populate the common
`forge:gems`, `forge:ingots`, and `forge:plates` tags respectively. Tags append using the default `replace: false`.
Recipes use specific material/component tags, including Forge's vanilla ingredient tags, so equivalent
ingredients can be supplied by data packs or other mods. Framed crystals cannot replace loose crystals.
Generic ingots or plates are never accepted where Phase Alloy is required.

## Assets and generation

Each item has an original transparent 32×32 PNG in `src/main/resources/assets/subspace/textures/item/`,
using the ID above as its filename. Item models use `minecraft:item/generated`; these are flat inventory
sprites with native Minecraft item thickness. The crystal's luminous appearance comes from contrasting
violet/cyan facets; the items have no dynamic lighting or custom renderer.

The built-in image-generation sources and exact prompts are saved in `docs/art/components/`. Run
`python tools/assets/convert_component_textures.py` with the authoring dependencies in
`tools/assets/requirements.txt` to reproduce all five textures and `build/component-texture-preview.png`.
The converter fits the opaque source silhouette within 28 pixels on a 32×32 canvas, samples with nearest
neighbor, snaps to the shared palette, and thresholds alpha to 0/255. It uses no smoothing or dithering.

`SubspaceData` registers providers for names, item models, recipes, recipe-book advancements, and item
tags. Their output lives in `src/generated/resources` and is committed. Run `./gradlew runData`, then
a separate `./gradlew clean build`; ordinary builds use the committed assets and require no Python.
Use `./gradlew.bat` in PowerShell. No new blockstate, GUI, animation, or particle asset is needed for
these static items.

## Manual checks

Install the current release JAR with Forge 43.5.2 / Minecraft 1.19.2 / Java 17. Verify the five item
names and sprites in the creative tab, held views, and dropped-item views. Craft each recipe above,
including the three-ingot output and vertical plate recipe. Check recipe-book discovery while obtaining
ingredients normally. Confirm that vanilla iron cannot substitute for Phase Alloy in the plate recipe,
and that a loose Phase Crystal cannot substitute for a stabilized one in the coil recipe.

The automated build and file checks do not launch Minecraft. In-game checks are reserved for the owner.
