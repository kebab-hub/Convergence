# Subspace Anchor

`subspace:subspace_anchor` is a local stable endpoint with a persistent, owner-independent UUID network
address. It has no inventory, transport, routing, network membership, or chunk-loading behavior.

## Interaction

Place the anchor to create an active endpoint. Right-click it to open its status screen:

- **Network address:** the complete UUID, split over two lines for readability.
- **Status:** `Active / stable` or `Inactive`.
- **Linked endpoints:** `0`. The Phase Linker currently records block-face addresses only; it does not
  create links or change this count. Link membership is a later feature.
- **Activate / Deactivate:** changes the local state without changing the address. Inactive anchors
  retain their identity. There is no fuel, power requirement, or redstone control in this slice.

The address belongs to the placed anchor, not its placer. Other players can inspect the same address
and operate the control subject to normal interaction/build restrictions. Spectators can inspect only.
Sneaking leaves normal adjacent-block placement available. Holding a Phase Linker selects the anchor's
block face instead of opening this screen, using the linker's existing behavior.

Active anchors illuminate nearby blocks at light level 10, show luminous violet/cyan surfaces, and emit
gentle crystalline particles. Inactive anchors use dim textures and stop emitting new particles/light;
already spawned particles finish their short fade. Switching state plays a quiet amethyst chime.
The suspended core is JSON geometry, with no block-entity renderer or animation ticker.

## Identity and lifecycle

One UUID is the unique network address; there is no separate owner UUID or redundant address number.
The server creates it on placement/initialization. The block entity saves a versioned `Identity` compound
containing `address`, `dimension`, and packed `pos`; `active` is persisted in the block state. Regular
save/reload, chunk unload/reload, and active/inactive switching preserve the address.

Breaking the anchor drops an ordinary block item when mined with a pickaxe. Its next placement gets a
new UUID. Placing a copied item also creates a new UUID even if its `BlockEntityTag` carries old data.
Structure/command copies at another position or dimension reidentify because the saved location no longer
matches. Missing/malformed/unknown-version identity data is repaired with a new UUID, never an all-zero
address. No existing network memberships need migration because none are implemented.

The UUID prevents accidental address collisions; it is not an access-control secret. These rules cover
normal placement and copies within a world, not an administrator deliberately editing world data to
forge the same UUID at the same saved location. Anchors cannot be moved by pistons.

`AnchorIdentity` handles persistence and location binding. `SubspaceAnchorBlockEntity` owns the local
identity; `SubspaceAnchorBlock` owns state and visible feedback. `AnchorMenu` provides a slotless screen
using vanilla menu data/button packets. `AddressWords` transports all 128 UUID bits as eight 16-bit
words to avoid truncation by vanilla's signed-short data packets. A ready flag hides partially synced
addresses. No custom packet channel or topology service is added.

Button requests validate the button ID, current block-entity instance, dimension, distance (eight blocks),
world border, spectator/build permissions, and interaction permission. A replaced, removed, or unloaded
anchor cannot be controlled through an old screen. Validation only inspects already loaded chunks.
The screen's client registration and rendering remain isolated from common/server code.

## Crafting

One anchor uses four Phase Alloy Plates (`P`), two copper ingots (`C`), two Resonance Coils (`R`), and one
Stabilized Phase Crystal (`S`). Existing component/Forge tags are accepted; obtaining a coil unlocks it.

```text
PCP
RSR
PCP
```

## Assets and verification

- Six finished opaque 32×32 PNGs: black-metal frame, copper fittings, active/inactive violet core,
  and active/inactive cyan stabilizers.
- Two 23-element JSON models, both `active` blockstates, an inventory block-item model, self-drop loot,
  a pickaxe mining tag, recipe, advancement, and localized screen text.
- A finished 256×256 GUI atlas, with a 236×166 panel. Minecraft renders localized text and its native button.
- Existing `subspace_spark` particles and vanilla amethyst sounds are reused; no missing effect assets.

The original generated material atlas and [prompt](art/anchor/prompt.txt) are saved in `docs/art/anchor/`.
See [Asset Sources](ASSET_SOURCES.md#subspace-anchor--2026-11-01). Reproduce using the project's pinned Pillow version:

```sh
python tools/assets/generate_anchor.py
python tools/assets/preview_anchor.py
./gradlew runData
./gradlew clean build
```

The preview tool inspects the actual JSON geometry and UVs offline; it is not a Minecraft screenshot.
`build` includes resource validation and `verifyAnchorIdentity` for compressed NBT persistence, new/copy
identity, malformed-data recovery, and UUID synchronization through signed-short packet values.
`AnchorGameTests` covers block-entity lifecycle, toggling, and stale/distant menu rejection; it is compiled
but was not executed. No Minecraft client or gameplay server was launched for this delivery.

## Owner's in-game checks

Install the current normal `subspace-0.0.7.jar` on Minecraft 1.19.2 / Forge 43.5.2 with Java 17.

1. Craft an anchor or use `/give @s subspace:subspace_anchor`. Inspect inventory, held/dropped appearance,
   the frame/core from every side, lighting, particles, and adjacency to solid blocks.
2. Place two anchors. Open each screen and confirm distinct complete UUIDs, active status, and zero links.
   The entire address must agree when a second player opens the same anchor.
3. Toggle activation, close/reopen the screen, and save/reload. Status and UUID must persist; particles,
   brightness, and the chime must match the state. Test two viewers to check live status synchronization.
4. Unload and revisit the chunk: the same anchor keeps its UUID. Break/re-place it or copy it to another
   position using operator tools: the new anchor gets a different UUID. Mining with a pickaxe drops it.
5. While a screen is open, have another player remove or replace the anchor, or move out of range. The
   screen must close and cannot toggle the replacement. Spectators must not toggle it.
6. Select anchors with the Phase Linker. This must keep existing A/B behavior and leave linked counts at
   zero. No items or other resources should move.

These are manual acceptance checks for the owner; automated compilation does not prove client rendering.
