# Subspace Engineering — Art Style

## Direction

Industrial sci-fi machinery built to contain an invisible spatial phenomenon: heavy dark frames,
precise copper/brass hardware, cyan control electronics, and vivid violet/magenta energy around luminous
crystalline cores. Aim for the finish and readability of a high-end Minecraft tech mod while making
original assets with a consistent identity.

All block and item textures use **32×32 Minecraft pixel art**. The foundation includes an original
32×32 creative-tab emblem and its GUI atlas, with no demonstration blocks or items. GUI atlases may
be larger to fit native Minecraft layouts; the 512×512 tab atlas preserves the emblem at 32×32 pixels.
See [Asset Sources](ASSET_SOURCES.md) for provenance and reproduction instructions.

## Palette and material roles

These starting swatches guide consistency rather than prescribing every pixel.

| Role | Suggested colors | Treatment |
| --- | --- | --- |
| Near-black frame | `#101319`, `#1B2028` | Deep seams, supports, recesses; preserve detail above pure black. |
| Gunmetal panels | `#2C3440`, `#465263`, `#657184` | Broad restrained surfaces, beveled highlights, deliberate wear. |
| Copper/brass hardware | `#74432B`, `#B8753E`, `#D6AA59` | Fasteners, coil windings, brackets, contacts. |
| Cyan electronics | `#087F98`, `#20CEDB`, `#A4F8FF` | Status lights, circuits, controls, information flow. |
| Subspace energy | `#6425B7`, `#AE3FFF`, `#F33CCF` | Contained fields, core facets, aperture edges. |
| Crystal highlights | `#EACDFF`, `#FFF0FF` | Small luminous facets; brightest pixels reserved for the core. |

Dark metal dominates; hardware frames the mechanism, cyan explains its controls, and violet/magenta
identifies subspace activity. Avoid covering whole faces with bright pixels. Distinguish cyan from
violet through placement and shapes as well as hue.

## Pixel craft

- Work on a native 32×32 grid with crisp clusters, stepped diagonals, and a few intentional shade levels.
  Avoid blur, automatic antialiasing, smooth gradients, and photographic noise.
- Use broad flat metal panels, warm segmented coils, and angular crystal facets with tiny bright highlights.
  Concentrate wear at exposed corners rather than random speckling.
- Make functional faces legible at normal play distance: a connector, control panel, or core window should
  read before individual rivets. Keep icons recognizable against light and dark inventory slots.
- Keep lighting and edge highlights consistent. Avoid baked shadows that contradict model geometry.
- Use transparency deliberately for silhouettes and apertures. Inspect alpha edges with nearest-neighbor
  scaling; opaque metal faces must not contain accidental transparent pixels.

## Physical models

Use Minecraft JSON model geometry where depth improves readability: recessed panels, raised rims,
copper brackets, sockets, core cages, and inset crystalline housings. Texture shading supports that
depth. Full cubes remain appropriate for simple structural blocks.

- Keep geometry economical and aligned. Avoid coplanar overlapping faces, hidden excess elements,
  and excessive microgeometry. Stay within the intended block footprint unless a feature requires otherwise.
- Match UVs to the 32×32 pixel grid. Standard Minecraft model coordinates/UVs use a 0–16 convention;
  a full 32-pixel face still maps across 0–16 UV units.
- Check face culling, rotation, ambient occlusion, particle textures, and inventory transforms.
- Prefer ordinary block/item JSON models. Introduce custom rendering only for a concrete effect those
  models cannot express, isolated from common/server code.

Luminous crystals read as bright through palette and contrast. Bright PNG pixels alone do not emit
world light or guarantee emissive rendering. If a feature needs those effects, deliver its light/render
behavior and assets together; the basic appearance must work without an external shader/resource pack.

## Family resemblance and states

Interfaces emphasize connector faces and a small core; network controls emphasize readable panels;
pocket-space and portal equipment can emphasize a contained aperture. These are future visual directions,
not a list of blocks to register now.

Reuse frame proportions, fasteners, connectors, and palette across related equipment. Distinguish roles
by silhouette and face layout, not recoloring alone. Show inactive, active, overloaded, or unavailable
states only when the mechanic has them. Pair color changes with patterns, shapes, or readable status
text; never communicate a fault by hue alone.

For animated textures each frame remains 32×32; a vertical PNG strip may therefore be taller than 32 pixels.
Include its `.png.mcmeta`, use restrained timing, and avoid rapid full-face flashing.

## Required delivery for every visible block/item

**Actual assets must be committed with the content in the same task. Art descriptions and future-art
TODOs are not deliverables. Do not register content with missing textures or temporary vanilla stand-ins.**

1. Original 32×32 PNG textures under `src/main/resources/assets/subspace/textures/block/` or `textures/item/`
   (animation sheets follow the per-frame rule above).
2. Working JSON models under `assets/subspace/models/block/` and/or `models/item/`. A block item may inherit
   its block model; a flat item still needs a model referencing its actual PNG.
3. For blocks, blockstates connecting every exposed state/orientation to valid models and an inventory
   item model. Include particle textures and appropriate display transforms.
4. Names in `assets/subspace/lang/en_us.json`, plus relevant loot, tags, and recipe data. Generated JSON
   can live under `src/generated/resources` and must be committed.
5. Verification that resource IDs resolve, texture dimensions are correct, no faces z-fight or disappear,
   and the block/item looks correct in inventory and in-world in supported states and orientations.

Review assets under normal Minecraft lighting at actual game scale, and inspect pixels with nearest-neighbor
enlargement. If client inspection cannot run, report the gap; compilation alone is not visual validation.
Record provenance/license for third-party source assets. Other tech mods are mood references, never a
source of copied textures.
