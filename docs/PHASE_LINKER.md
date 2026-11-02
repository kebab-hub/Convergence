# Phase Linker

`subspace:phase_linker` is a nonstackable handheld address-selection tool on the Subspace Engineering
creative tab. It records two block faces. It does not create a network, transfer resources, inspect
capabilities, reserve endpoints, or establish ownership.

## Use

1. Right-click a block to record endpoint **A**: dimension, block coordinates, and clicked face.
2. Right-click a different address to record **B**. Another face of the same block is a distinct address.
3. Once both are recorded, the next valid block click starts a new **A** and removes the old **B**.

Hover over the tool to see both full addresses and the next action. Right-click air to receive private
chat lines with the current availability of each saved endpoint. Sneak-right-click a block or air to
clear the pair, except on a Subspace Interface as described below. A short four-tick cooldown prevents accidental rapid input.

**Subspace Interface configuration:** select the local inventory-port face on two [Interfaces](INTERFACE.md),
then sneak-use both selected interfaces to confirm a direct item pair. Each local interaction applies the
other endpoint; the tool retains A/B. Attach inventories to those selected faces and automation to other
faces. With only A selected, sneak-use still applies A as the local interface target. Clear an interface
with empty-hand sneak-use, or clear the tool by sneak-right-clicking air. Normal use still records A/B.

Successful selections produce violet sparks at the selected face and an amethyst chime; B uses a higher
pitch. The item handles block selection before normal block interaction, so selecting a chest does not
open it and selecting a detector does not trigger a reading. Keep the other hand empty when testing an
offhand linker: normal Minecraft main-hand interaction priority still applies.

## Unavailable and invalid endpoints

- Selection is server-authoritative. New targets must be loaded, non-air, inside build/world-border
  bounds, and permitted by the world's interaction check. Normal Minecraft interaction range applies.
- An exact duplicate A/B, invalid target, or protected position leaves the current selection unchanged.
- Stored addresses survive dimension changes, chunk unloads, removed blocks, and item save/load. B can
  be selected while A is unavailable. The completion message reports A's current availability.
- Status distinguishes an available block, an unloaded chunk, a missing dimension, an out-of-bounds
  position, and a missing block. It only examines already loaded chunks; it never requests a load.
- The address identifies a location and face, not a particular block instance. Replacing a removed
  block at that address makes it available again. Availability is informational, not transport validation.
- Malformed saved data is displayed as invalid and preserved until explicitly cleared. Selection does
  not silently replace it. Clearing removes only the linker's own NBT and preserves other item data.

## Crafting

One copper ingot (`C`), one Stabilized Phase Crystal (`S`), one Resonance Coil (`R`), and two Phase Alloy
Ingots (`I`) craft one linker in a crafting table. Spaces below are empty slots; the recipe also mirrors.

```text
 CS
 RI
I
```

The recipe unlocks upon obtaining a Stabilized Phase Crystal. Existing component tags and the Forge
copper-ingot tag are accepted. Models, names, recipe, and recipe-book advancement are generated with
`./gradlew runData`; commit that output before building.

## Persistence and code boundaries

`PhaseLinkerItem` handles user interaction and feedback. `Endpoint` serializes an immutable address;
`LinkerSelection` handles the A/B cycle and strict NBT reads; `EndpointAvailability` performs transient
server checks without retaining world/chunk references. These small helpers contain no transport logic.

Item NBT contains only the following owned subtree (B is absent until selected):

```snbt
subspace_linker: {
  a: {dimension: "minecraft:overworld", pos: {x: 12, y: 64, z: -8}, face: "north"},
  b: {dimension: "minecraft:the_nether", pos: {x: 3, y: 70, z: 9}, face: "up"}
}
```

Dimension IDs must be fully qualified valid resource IDs, coordinates must be NBT integers, and faces
must be lowercase vanilla direction names. B without A and identical A/B records are invalid. There is
no saved selection mode, availability, block type, network ID, or extra gameplay data. A later schema
change must explicitly handle these original address records.

## Assets and validation

The finished texture is `src/main/resources/assets/subspace/textures/item/phase_linker.png` (32×32,
binary alpha, shared palette). Datagen supplies a `minecraft:item/handheld` model. Original art was
made with built-in image generation; [source](art/phase_linker/source.png) and [exact prompt](art/phase_linker/prompt.txt)
are saved with the [provenance record](ASSET_SOURCES.md#phase-linker--2026-11-01).

```sh
python tools/assets/convert_phase_linker.py
./gradlew runData
./gradlew clean build
```

The converter uses the project's pinned Pillow version and produces a light/dark preview under `build/`.
`build` runs asset validation and `verifyLinkerSelection`, a plain Java check of A/B transitions, all six
faces, custom dimension IDs, immutable positions, compressed NBT save/load, malformed data, and safe
clearing. It uses Minecraft's value/NBT classes without starting a game. `PhaseLinkerGameTests` covers
item interaction, invalid selection, persistence, and no-load availability checks; `build` compiles these
tests but does not execute them. No Minecraft client or gameplay server was launched for this delivery.

## Owner's in-game checks

Use the current normal `subspace-0.0.6.jar` with Minecraft 1.19.2, Forge 43.5.2, and Java 17.

- Craft the linker or use `/give @s subspace:phase_linker`. Check its name, creative-tab placement,
  32×32 inventory sprite, held/dropped appearance, and maximum stack size of one.
- Select two blocks and inspect both addresses/faces in the tooltip. Check A/B messages, sparks,
  and differing chime pitches. Try all six faces, including two faces of one block.
- Try the exact same face twice: A must remain unchanged. After a complete pair, select another block:
  it must become the new A with B empty. Sneak-right-click block and air to clear.
- Select a chest/detector without opening/activating it. Check the offhand tool with an empty main hand.
- Carry A through a portal and select B in another dimension. Right-click air to check status. Travel far
  enough to unload A, check again, then return; both saved addresses must survive without chunk loading.
- Break a selected block and check for a missing-block status; replace it and check availability again.
  Save/reload the world and move/drop/recover the item: the pair must persist.
- Optional operator check: give a malformed linker using
  `/give @s subspace:phase_linker{subspace_linker:{a:{face:"diagonal"}}}`. Its tooltip must explain the
  invalid state, normal selection must preserve it, and sneak-right-click must recover the tool.

These are owner-run acceptance checks, not claims of automated in-game validation.
