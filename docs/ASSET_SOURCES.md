# Asset Sources

## Subspace Interface — 2026-11-01

- **Origin:** Original material atlas created with built-in image generation, without external references
  or copied textures. [Source](art/interface/materials_source.png) and [exact prompt](art/interface/prompt.txt).
- **Finished files:** `src/main/resources/assets/subspace/textures/block/subspace_interface_*.png`:
  frame, copper coupler, connected/disconnected crystal core, and connected/disconnected cyan display.
  Each is an opaque 32×32 PNG using the shared palette.
- **Conversion:** `tools/assets/generate_interface.py` crops the actual tile boundaries (the horizontal
  source seam is at 48% height), samples with nearest neighbor, and maps to the palette. Disconnected
  variants preserve the art with dimmed palette substitutions; no smoothing or replacement drawings.
- **Models/data:** Two 13-element models contain raised dark rails, copper collars, a recessed socket,
  crystal, and status indicators. Connected core/indicator fronts are emissive. Eight variants cover four
  facings and two connection states. Inventory uses the disconnected model. The generator also writes loot;
  datagen owns translations, recipe/advancement, and the shared mining tag.
- **Inspection:** Viewed native/enlarged textures and both JSON geometry/UV renders using the existing
  offline renderer. No Minecraft was launched. No new particles, sounds, GUI, or animation sheet are needed;
  applying a link reuses Minecraft's amethyst chime. See [Interface](INTERFACE.md) for manual checks.

## Subspace Anchor — 2026-11-01

- **Origin:** An original four-quadrant material atlas created with built-in image generation. No external
  references or copied game textures were used. [Source](art/anchor/materials_source.png) and
  [exact prompt](art/anchor/prompt.txt) are committed with the assets.
- **Finished textures:** `src/main/resources/assets/subspace/textures/block/subspace_anchor_*.png`.
  `tools/assets/generate_anchor.py` crops each material quadrant, resamples to native 32×32 with nearest
  neighbor, and maps to the shared palette. Inactive core/light variants preserve the same art with
  dim palette substitutions. All six textures are opaque; no smoothing or replacement drawing is used.
- **Models:** The same generator writes active/inactive 23-element JSON models, two blockstates, inventory
  model, and loot. An open black-metal frame and copper clamps surround a rotated floating crystal;
  cyan stabilizers sit on the base. Only active core/stabilizer faces are emissive.
- **GUI:** `textures/gui/anchor.png`, a 256×256 atlas with a native 236×166 panel, is drawn using the existing
  palette and pixel geometry conventions. It incorporates a small core detail from the generated art;
  Minecraft supplies localized text and its native button. No text is baked into the PNG.
- **Effects:** Existing original `subspace_spark` art and the vanilla amethyst chime are reused. Active
  feedback uses particles rather than a separate animated sheet or custom block-entity renderer.
- **Inspection:** Viewed 32×32 textures, enlarged active/inactive geometry/UV renders, and a GUI layout
  preview. `tools/assets/preview_anchor.py` reproduces the latter previews without launching Minecraft;
  its lighting/font are approximate. Client rendering remains the owner's manual check.

## Phase Linker — 2026-11-01

- **Origin:** Original transparent probe sprite generated with the built-in image-generation tool.
  No external reference images, copied game art, or third-party textures were supplied.
- **Source and prompt:** [Full 1254×1254 source](art/phase_linker/source.png) and
  [exact generation prompt](art/phase_linker/prompt.txt).
- **Finished asset:** `src/main/resources/assets/subspace/textures/item/phase_linker.png`.
  `tools/assets/convert_phase_linker.py` crops to alpha coverage, preserves the aspect ratio within
  28 pixels, uses nearest-neighbor sampling, maps to the shared 21-color palette, and thresholds alpha
  at 128. The final centered canvas is 32×32; there is no smoothing, dithering, or replacement drawing.
- **Model/effects:** Datagen supplies `minecraft:item/handheld`. Selection reuses `subspace_spark` and
  Minecraft's amethyst chime, with distinct A/B pitches. No additional effect assets are required.
- **Inspection:** Viewed the final native 32×32 and reduced 16×16 sprite with enlarged light/dark
  previews. No Minecraft client or gameplay server was launched. See [Phase Linker](PHASE_LINKER.md).

## Subspace Detector — 2026-11-01

- **Origin:** Original front, side, and top face art made with the built-in image-generation tool;
  a built-in edit of the front made the inactive state. No third-party textures or reference images
  were supplied. All four originals are in `art/detector/*_source.png`.
- **Prompts:** [Generation prompts](art/detector/prompts.json) and [inactive edit prompt](art/detector/inactive_prompt.txt).
- **Finished PNGs:** `src/main/resources/assets/subspace/textures/block/subspace_detector_{front_active,front_inactive,side,top}.png`.
  Each is opaque 32×32. `tools/assets/generate_detector.py` uses nearest-neighbor conversion and the
  shared palette, without smoothing or replacement drawings.
- **Models:** The same tool writes two 18-element instrument models, their 16 blockstate combinations,
  the inactive item model, and self-drop loot. Rails, fasteners, bezels, socket, and sensor have physical
  depth; only active crystal/screen faces are emissive. There is no custom renderer or animation sheet.
- **Effects:** The detector reuses the original `subspace_spark` sprite below and Minecraft's existing
  amethyst chime sound event. No copied sound files or new particle resources are needed.
- **Inspection:** Inspected native/enlarged textures and an offline geometry render of both JSON models.
  Minecraft was not launched. See [Detector](DETECTOR.md) for reproduction and manual checks.

## First five components — 2026-10-31

- **Origin:** Five original sprites made with the built-in image-generation tool, one call per item.
  No external images or copied mod/vanilla textures were supplied. The full prompt set is saved in
  [`art/components/prompts.json`](art/components/prompts.json).
- **Sources:** `art/components/<item_id>_source.png` contains each selected original: `phase_crystal`,
  `stabilized_phase_crystal`, `resonance_coil`, `phase_alloy_ingot`, and `phase_alloy_plate`.
  Crystal/coil source canvases are 1254×1254; alloy sources are 1536×1024. Original alpha is retained in
  these source files; they are documentation assets and are not packaged in the mod JAR.
- **Finished assets:** `src/main/resources/assets/subspace/textures/item/<item_id>.png`, exactly 32×32.
  [`convert_component_textures.py`](../tools/assets/convert_component_textures.py) preserves the source
  silhouette and aspect ratio, fits it within 28 pixels, uses nearest-neighbor sampling, maps colors to
  `palette.json`, and thresholds alpha at 128. No smoothing, dithering, or procedural replacement art.
- **Inspection:** The finished sprites were inspected on dark and light inventory backgrounds, enlarged
  with nearest-neighbor scaling and at native 32×32 / reduced 16×16 sizes. In-game testing remains with
  the project owner. See [Components](COMPONENTS.md) for generation commands, IDs, recipes, and checks.

## Decorative test casing and spark — 2026-10-31

- **Reference:** [subspace_test_casing_reference.png](art/subspace_test_casing_reference.png), original
  art made with the built-in image-generation tool using the [saved prompt](art/subspace_test_casing_prompt.txt).
  No external images or copied mod textures were supplied.
- **Conversion:** The reference's dark structural rails, recessed aperture, copper retainers, cyan service
  indicators, and faceted violet core were redrawn as native pixel clusters in
  `tools/assets/generate_test_casing.py`. Perspective concept pixels are not pasted onto block faces.
- **Finished textures:** `src/main/resources/assets/subspace/textures/block/subspace_test_casing_*.png`:
  side, top, front, and frame are opaque 32×32 PNGs; core is four 32×32 frames in a 32×128 sheet with native
  `.png.mcmeta`. All colors come from `tools/assets/palette.json`.
- **Particle:** `textures/particle/subspace_spark.png` under the same asset root is an original 32×32
  crystalline spark drawn from that palette, with intentional transparent surroundings.
- **Models:** The authoring script writes the reusable recessed-casing model, concrete block/item models,
  four facing variants, sprite definition, and loot. Shared mining tags now come from datagen. Geometry uses ten elements, with physical
  recesses and retainers. Only the crystal face is emissive; the casing also emits a constant light level.
- **Reproduction:** Follow [Asset Pipeline](ASSET_PIPELINE.md). Finished resources and the full reference
  are stored in the repository. The tool creates `build/asset-preview.png` for pixel inspection.
- **Inspection:** Native pixel art and enlarged frames were inspected as files. In-game rendering is
  reserved for the project owner; no Minecraft client or gameplay server was launched for this task.

## Native creative-tab atlas

- **File:** `src/main/resources/assets/subspace/textures/gui/creative_tabs.png`
- **Origin:** Original panel and scrollbar geometry drawn by `tools/GenerateCreativeTabAtlas.java`,
  using the finished emblem below and the project palette. No vanilla or third-party textures are copied.
- **Layout:** 512×512 PNG, representing the native 256×256 logical atlas at double pixel density.
  Each logical 16×16 icon retains all 32×32 source pixels. Six tab columns cover all placements;
  four rows provide top/bottom and selected/unselected variants. Both scrollbar thumbs are included.
- **Reproduction:** From the repository root, run `java tools/GenerateCreativeTabAtlas.java` with Java 17.
  Commit the generated PNG alongside any source-emblem or generator changes. This tool uses only the JDK
  and does not launch Minecraft. Language JSON is generated separately with `./gradlew runData`.

## Subspace creative-tab emblem

- **File:** `src/main/resources/assets/subspace/textures/gui/subspace_emblem.png`
- **Purpose:** GUI identity art for the Subspace Engineering creative tab; it does not represent a gameplay item.
- **Origin:** Original art generated on 2026-10-26 with the built-in `image_gen` tool. No reference images, third-party textures, or copied game assets were used.
- **Generation record:** `exec-f2390eb0-0eb4-4ab7-a24f-8376df54070e.png`, generated as a transparent 1254×1254 RGBA source. The source remains in Codex's generated-image storage; the finished asset is checked into this repository.
- **Production:** Reduced the complete source to exactly 32×32 with nearest-neighbor sampling, without cropping or redrawing. Converted alpha below 128 to 0 and all remaining alpha to 255. Mapped opaque RGB pixels to the nearest color in the 22-color palette below by squared RGB distance, without dithering; 21 opaque colors appear in the finished sprite. Transparent RGB is zeroed. No blur, resampling interpolation, or procedural replacement art was used.
- **Inspection:** Viewed the final native 32×32 sprite, a nearest-neighbor 16×16 reduction, and 10× enlargements against light and dark inventory-like backgrounds. Verified binary alpha, 637 opaque pixels, and one connected silhouette. Minecraft client inspection was not run.

Production palette (hex): `101319 1B2028 2C3440 465263 657184 8D9CB5 74432B B8753E D6AA59 ED8E45 FFE19B 087F98 20CEDB A4F8FF 351347 471078 6425B7 AE3FFF F33CCF C373EB EACDFF FFF0FF`.

### Generation prompt

```text
Use case: stylized-concept
Asset type: final 32 by 32 pixel Minecraft inventory sprite, a plain creative-tab identity emblem for original mod Subspace Engineering.
Primary request: ONE polished tiny futuristic industrial emblem with a compact dark gunmetal beveled octagonal housing, precisely placed copper/brass retainers, small cyan circuit nodes, and a vivid violet/magenta faceted luminous crystalline core.
Scene/backdrop: completely transparent outside the icon, no drop shadow or backdrop.
Style/medium: authentic hand-placed low-resolution pixel art. Design ON A STRICT NATIVE 32 BY 32 PIXEL GRID. If the canvas is larger, make each source pixel an exact flat square uniformly magnified, like a 32x32 sprite enlarged to 1024x1024 using nearest neighbor. There must be only 32 logical pixel columns and rows. No subpixel elements.
Composition: single frontal emblem centered, symmetrical readable eight-sided silhouette spanning x=2..29 and y=2..29 on a 32x32 logical canvas. Generous flat dark gunmetal structure surrounds a single angular crystalline magenta core. Four compact warm retainers hold the core. Two cyan status nodes offset in housing. Chunky clear forms, clear silhouette even at 16x16 inventory size.
Palette/materials: restrained 18-color palette. Near-black #101319 and #1B2028 seams, gunmetal #2C3440 #465263 #657184, copper #74432B #B8753E and brass #D6AA59, electronics #087F98 #20CEDB #A4F8FF, crystalline core #6425B7 #AE3FFF #F33CCF with tiny #EACDFF #FFF0FF highlights. Dark metal dominates. Consistent upper-left lighting. Facets use bold flat pixel clusters, bright core physically contained by housing.
Constraints: genuinely crisp square pixel clusters, stepped diagonals, deliberate two/three-value metal bevels, opaque interior, binary transparency at silhouette. An original finished game asset, high-end Minecraft tech aesthetic. No lettering, labels, symbols, words, text, watermark, repeated variants, grid lines, pedestal, background, mockup sheet, diffuse glow, blur, antialiasing, gradients, photographic texture, random noise, thin lines, or fine details smaller than one logical pixel.
```
