# Visual Asset Pipeline

This pipeline targets Minecraft 1.19.2 / Forge 43.5.2. It adds one decorative reference block,
`subspace:subspace_test_casing`, and a reference particle, `subspace:subspace_spark`.
The casing has no inventory, block entity, menu, recipe, ticking, network, or subspace behavior.
Its constant light level of 7, animated crystal, and horizontal facing are visual features only.

## Palette and authoring helpers

- [`tools/assets/palette.json`](../tools/assets/palette.json) is the reusable named color source.
  It follows [Art Style](ART_STYLE.md): gunmetal, dark recesses, copper/brass, cyan controls, violet energy.
- [`palette.gpl`](../tools/assets/palette.gpl) is the generated palette for compatible pixel editors.
- [`common.py`](../tools/assets/common.py) supplies a 32×32 RGBA canvas, named colors, crisp integer-pixel
  rectangles/lines/polygons, JSON writing, vertical animation packing, and projected model UVs.
- [`generate_test_casing.py`](../tools/assets/generate_test_casing.py) is the worked reference. It redraws
  the image-generated concept as native pixels and geometry; it does not shrink a perspective picture
  onto a cube. See [Asset Sources](ASSET_SOURCES.md) for the reference and prompt.

Authoring requires Python 3.10+ and the Pillow version in `tools/assets/requirements.txt`. The ordinary
Gradle build uses the committed PNG/JSON files and does **not** require Python or image generation.

From the repository root:

```sh
python -m pip install -r tools/assets/requirements.txt
python tools/assets/generate_test_casing.py
./gradlew runData
./gradlew clean build
```

Use `./gradlew.bat` in PowerShell. Run datagen before a separate build so changed generated resources
are included. The authoring script also writes `build/asset-preview.png` for inspection; `clean` removes
that preview. Re-running the script reproduces the checked-in casing assets and editor palette.

## Files and ownership

| Resource | Location / convention |
| --- | --- |
| Block textures | `src/main/resources/assets/subspace/textures/block/<block>_<surface>.png` |
| Standalone item textures | `assets/subspace/textures/item/<item>.png`, under the same root |
| Particle sprites | `assets/subspace/textures/particle/<particle>.png` |
| Reusable model geometry | `assets/subspace/models/block/template/<family>.json` |
| Concrete block model | `assets/subspace/models/block/<block>.json` |
| Block item model | `assets/subspace/models/item/<block>.json` |
| Blockstates | `assets/subspace/blockstates/<block>.json` |
| Particle sprite lists | `assets/subspace/particles/<registered_particle>.json` |
| Language | `SubspaceLanguageProvider` → `src/generated/resources/assets/subspace/lang/en_us.json` |
| Loot and tags | `src/main/resources/data/<namespace>/...` for this reference block |

The casing authoring script owns its textures, template, models, blockstate, loot/tag JSON, and spark
sprite/definition. Change the script or palette before regenerating those files; otherwise regeneration
will replace manual edits. Language is owned by datagen. Do not duplicate a resource in `main` and
`generated`, or manually edit generated language output. Future assets can use the helpers without
becoming outputs of this casing-specific script.

## Texture conventions

- Static block/item/particle PNGs are exactly **32×32**. Use named palette colors and integer pixel clusters.
  Keep metal opaque; use deliberate transparency for free-standing sprites. No blur or antialiasing.
- Pixel helper rectangle bounds are inclusive. Model coordinates are separate: one full block face spans
  **0–16 model/UV units**, even with 32×32 pixels. Half-unit UV steps select single pixels.
- Use stable lowercase `snake_case` resource IDs. Model texture IDs omit `textures/` and `.png`:
  `subspace:block/subspace_test_casing_side`.
- Large GUI atlases follow their native layout; the 32×32 rule applies to individual icons/frames rather
  than the whole GUI sheet. Existing creative-tab art is unchanged by the casing generator.

## Block model conventions

`subspace:block/template/recessed_casing` is a reusable north-facing template with five texture slots:
`side`, `top`, `front`, `frame`, and `core`. `particle` resolves to `#side` for mining/break effects.
The concrete casing model assigns those slots; other decorative blocks can reuse the geometry.

The body sits behind four outer rails and four copper retainers. A small crystal insert is slightly
raised from the recessed front panel. All elements remain inside 0–16, with a normal full-cube collision
shape. The crystal insert deliberately maps a complete 32×32 texture onto its smaller surface for
readable facets; other faces use projected UVs from their bounds.

Only faces on the external block boundary have `cullface`. Inset faces must stay visible next to blocks.
Omit backs that are hidden against the body, avoid coincident visible faces, and use the template's
`minecraft:block/block` parent for standard inventory/hand transforms. `FacingDecorativeBlock` provides
placement, inherited rotation/mirroring, and a `facing` property without machine logic. North is rotation
0; east/south/west use 90/180/270 degrees. Model rotation carries the directional artwork with the block.

The crystal's north face uses Forge 1.19.2's native `emissivity: 15`, disabled face ambient occlusion,
and an unshaded element. This keeps that face bright without a shader pack or custom renderer. World
illumination is separately supplied by the block's constant light level. Bright PNG colors alone do
not implement either effect.

## Item model conventions

Block items inherit their concrete block model:

```json
{ "parent": "subspace:block/subspace_test_casing" }
```

Future flat items should use `minecraft:item/generated` with `layer0: "subspace:item/<item>"`; handheld
tools may use `minecraft:item/handheld` when appropriate. Each must include its actual 32×32 PNG.
The casing is a normal `BlockItem` on the Subspace Engineering tab; its name uses the block translation.

## Animated textures

Use vertically stacked 32×32 frames and a sibling `<texture>.png.mcmeta`. `save_animation` packs frames
and validates dimensions, timing, and indices. The casing core has four frames in a 32×128 PNG, eight
ticks per frame, and sequence `[0, 1, 2, 3, 2, 1]` (a 48-tick cycle). It uses `interpolate: false` to
preserve pixel clusters. This is a slow decorative pulse, not an active/inactive gameplay state.
Animation is handled by Minecraft's texture atlas; it needs no ticker, packets, or block entity.

## Particle registration

`ModParticles` owns common `ParticleType` registrations and is attached by `ModRegistries.register`.
It must never import renderer classes. `ClientParticles` is a `Dist.CLIENT` mod-bus subscriber that
registers sprite factories through Forge 1.19.2's `RegisterParticleProvidersEvent.register` overload.

For a new sprite particle, add its type, isolated client provider, 32×32 sprite, and matching particle
JSON together. Particle JSON uses IDs such as `subspace:subspace_spark`; they resolve under
`textures/particle/`, unlike model texture IDs. No custom atlas loader is necessary.

The reference spark uses a finished violet crystalline sprite, full-bright lighting, supplied velocity,
and a 16-tick fade. The casing does **not** spawn it. It exists to verify the registration path manually:

```text
/particle subspace:subspace_spark ~ ~1 ~ 0.2 0.2 0.2 0.02 20 force
```

## Validation and manual checks

`./gradlew validateAssets` runs without Minecraft and is part of `build` via `check`. It checks duplicate
resource paths, JSON syntax, local model inheritance and texture slots, UV/bounds/culling conventions,
blockstate references, block translations, 32×32 textures, animation sheets/indices/timing, and particle
sprite paths. Vanilla model parents are supplied by Minecraft and are not copied into this repository.
Templates may leave texture slots open; validation resolves them through each concrete model.

File validation does not replace Minecraft rendering checks. The owner should verify:

- `subspace_test_casing` appears with the correct item model and name in the creative tab, or use
  `/give @s subspace:subspace_test_casing`.
- All four placement directions, recessed geometry, adjacent casings, inventory/held views, and mining
  particles render correctly. A pickaxe should recover the block; there is no crafting recipe.
- The core stays luminous in darkness, pulses slowly, and lights nearby surfaces.
- The particle command displays a small violet spark which fades; no particles appear automatically.

Do not launch Minecraft as part of this task's automated validation.
