# Subspace Interface

`subspace:subspace_interface` is the reusable foundation for a single direct remote capability link.
It persists a target dimension, block position, and face, and reports the target's connection state.
It probes standard Forge `ITEM_HANDLER`, `FLUID_HANDLER`, and `ENERGY` capabilities without naming or
depending on any other mod. It does not expose remote handlers, move resources, or route through networks.

## Configure and inspect

1. Select the remote block face as **endpoint A** with the Phase Linker. Clear the linker's previous pair
   first if necessary by sneak-right-clicking air.
2. Sneak-right-click the interface with that linker. This applies A as the single target, replacing any
   previous target. The linker's A/B selection is preserved; endpoint B is ignored for this operation.
3. Right-click the interface without the linker to read the full target address, connection state, and
   per-channel availability. Messages are private to the interacting player.
4. Sneak-right-click the interface with empty hands to clear its target. Sneaking with another held block
   leaves adjacent placement available.

Normal, non-sneaking linker use still selects A/B on any block, including interfaces. The interface does
not require an anchor, create anchor membership, or affect an anchor's linked endpoint count.
The configuration action checks the player's dimension, interaction distance, build permission, spectator
state, and normal world interaction permission. No claim is made that selecting a target authorizes future
remote resource access; access policy must be revalidated when transport is implemented.

## Connection state and lifecycle

`READY` means at least one supported capability is present on the selected face. It is a diagnostic state,
not a working transport connection. The recessed violet core and cyan indicators brighten in this state,
and the block emits light level 5. Its horizontal facing is visual; the saved target face is independent.

Other states distinguish no target, malformed saved data, a self-link, an unavailable dimension, an
out-of-bounds address, an unloaded chunk, a removed block, a missing block entity, another direct-link
host, unsupported capabilities, and a capability provider that throws during inspection.

Checks occur immediately on configuration/status reads and once per 20 server ticks, staggered by block
position. Presentation can lag a remote change by up to one second at normal tick rate. No capability
handle is cached, so invalidation, target replacement, side configuration changes, and revived providers
are detected on the next probe. The probe never calls insertion/extraction, fill/drain, or energy methods.

Unloaded/missing targets retain their address. Inspectors use existing server worlds and `getChunkNow`,
then check the existing block entity without creating one. They never load a target chunk or add tickets.
Configuration and state recover after reload; transient availability is recomputed, not saved as truth.
Self-links are rejected regardless of face. Links to another `DirectLinkHost` are rejected rather than
recursed into, including a target later replaced with an interface.

Breaking the interface drops its normal block item when mined with a pickaxe; the item does not carry
the configured target. Missing/malformed or future-version configuration is preserved, reported as invalid,
and inactive until explicitly cleared or replaced with a valid linker selection.

## Reusable boundaries

| Component | Responsibility |
| --- | --- |
| `Endpoint` | Existing immutable dimension/position/face value and strict NBT encoding. |
| `DirectLinkConfig` | Versioned single-target persistence; preserves malformed data for explicit recovery. |
| `DirectLinkHost` | Small configuration contract shared by direct-link hosts; also blocks recursive chains. |
| `CapabilityChannels.Channel<T>` | Typed Forge capability descriptor and sided presence probe; no mod-specific adapters. |
| `DirectLinkInspector` | Bounded server-thread, one-hop resolution of already available targets. |
| `ConnectionSnapshot` / `ConnectionState` | Immutable diagnostic results with no world, block entity, handler, or `LazyOptional` references. |
| `SubspaceInterfaceBlockEntity` | Thin host for configuration and periodic inspection; no `getCapability` forwarding. |
| `InterfaceInteraction` | Phase Linker application, permission checks, and translated player feedback. |

The saved block-entity subtree is:

```snbt
DirectLink: {
  version: 1,
  target: {dimension: "minecraft:overworld", pos: {x: 12, y: 64, z: -8}, face: "north"}
}
```

Future transport should reuse address resolution and typed channels, then add focused resource adapters.
It must obtain and validate a live target for each operation, enforce sided/access policy and shared
accounting, and invalidate any exposed proxy views on retarget/unload. A diagnostic snapshot is never a
capability lease. Returning a raw remote handler from `getCapability` now would allow pipes/hoppers to
transport resources before those protections exist, so this slice intentionally exposes none.
No custom packets, routing tables, topology registry, optional-mod imports, or speculative machine framework
are introduced. The only client synchronization is the visible `connected` block-state flag.

## Recipe and assets

One interface uses four Phase Alloy Plates (`P`), one copper ingot (`C`), two Resonance Coils (`R`), one
Stabilized Phase Crystal (`S`), and one Phase Alloy Ingot (`I`). The recipe accepts the existing component
and Forge tags and unlocks upon obtaining a coil.

```text
PCP
RSR
PIP
```

Six actual 32×32 textures supply the frame, copper coupler, and connected/disconnected core and displays.
Two 13-element JSON models provide raised rails, a recessed socket, copper collars, a crystalline core,
and cyan indicators. Eight blockstate variants cover both states and four facings. The disconnected model
is used in inventory. Names, status text, item model, self-drop loot, recipe, advancement, and mining tag
are included. There is no custom GUI, particle, or animation to require additional assets.

Original material art came from built-in image generation; the [source](art/interface/materials_source.png)
and [prompt](art/interface/prompt.txt) are committed. The authoring tool crops the actual material boundaries,
uses nearest-neighbor conversion to the shared palette, and derives dim states through palette substitution.
Its preview renders the actual geometry/UVs offline with approximate lighting, not through Minecraft.

```sh
python tools/assets/generate_interface.py
./gradlew runData
./gradlew clean build
```

Use the project's pinned Pillow version for authoring. The ordinary build needs only the committed assets.

## Validation and owner's in-game checks

`build` runs asset validation and `verifyDirectLinks`: compressed NBT round-trips, six faces, malformed
data preservation/clear, immutable snapshots, and item/fluid/energy probes against fake providers that
fail if any handler method is called. The checks invalidate and replace handles to exercise reacquisition.
They use real, unregistered Forge capability instances in the test harness because the production
`ForgeCapabilities` tokens require ModLauncher transformation. Actual standard-constant probes are covered
by the GameTests, which are compiled here and left for an explicitly requested server run.
`InterfaceGameTests` covers actual chest discovery without resource movement/proxy exposure, removed and
replaced targets, self-links/chains, unavailable targets without loading, and linker configuration. It is
compiled by build but was not run. No Minecraft client or gameplay server was launched for this delivery.

Install the current normal `subspace-0.0.8.jar` on Minecraft 1.19.2 / Forge 43.5.2 with Java 17:

1. Craft the interface or `/give @s subspace:subspace_interface`. Inspect inventory/held/dropped appearance,
   all four placement directions, recessed geometry, adjacency, and connected/disconnected lighting.
2. Select a chest face as A; sneak-use the linker on the interface. Confirm the exact dimension/coordinates/
   face and item capability in its status. Linker A/B must be unchanged. Try all six target faces.
3. Change the target, save/reload, and unload/revisit chunks. Targets must persist while availability changes.
   An unloaded target must remain unloaded. Cross-dimension targets should be reported without forcing loads.
4. Remove a target, replace it with stone, then a chest: expect missing block, no block entity, then ready.
   A replaced chest is a new provider at the same address; no old handler should remain in use.
5. Try self-links and interface chains: reject them without replacing a valid configuration. Clear with
   empty-hand sneak-use. Malformed NBT must show an invalid state and remain inert until reconfigured/cleared.
6. If compatible tech blocks are present, check their exposed fluid/energy sides. No hardcoded mod integration
   is required. Unsupported sides must not claim availability.
7. Attach hoppers/pipes and compare the target contents: no item/fluid/energy movement should occur. Anchors'
   linked counts stay zero. This task only delivers the direct-link foundation and diagnostics.
