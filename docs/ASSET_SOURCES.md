# Asset Sources

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
  four facing variants, sprite definition, loot, and mining tag. Geometry uses ten elements, with physical
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
