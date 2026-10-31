# Subspace Engineering — Progression

All system stages below are planned. The five [first components](COMPONENTS.md) and their vanilla-resource
crafting recipes are implemented; they do not yet unlock subspace abilities or a research tree.
Progression should teach one shared system at a time and expand what players can connect or build.
It should remain compact and playable with vanilla Minecraft and Forge alone.

## 1. Discover the layer

Introduce evidence of subspace and a readable way to identify potential connections. The exact discovery
method is undecided. The first components use amethyst, quartz, redstone, iron, and copper. Use accessible vanilla resources where practical;
do not begin by rebuilding an industrial material chain.

Player outcome: understand that subspace links real endpoints and has measurable limits.

## 2. Connect remote blocks

Introduce a small interface-and-link slice using item transport first. Teach endpoint selection, direction,
access, and connection status with two ordinary inventories. A full or unavailable destination must be
visible to the player and leave resources accounted for.

Player outcome: build a useful remote connection and understand why it may pause.

## 3. Engineer a network

Expand the same interfaces into explicit network membership, routing, filters, and a shared bandwidth
budget. Add fluid and energy transport through reusable capability adapters as their semantics are
validated. Stability makes network or spatial load a deliberate constraint; its exact cost model remains
to be designed. Overload should be legible and recoverable, with throttling or suspension preferred to
unannounced destructive effects.

Player outcome: improve capacity and reliability through configuration and infrastructure while retaining
the original interfaces.

## 4. Create pocket spaces

Introduce bounded spaces with anchors, persistent ownership, safe access, and a way back. Their connections
reuse network permissions and capacity accounting. Space allocation, unloaded behavior, and the consequences
of removing an anchor must be resolved before pockets become available to players.

Player outcome: arrange a compact remote workspace with predictable access and resource connections.

## 5. Fold distance

Introduce paired spatial endpoints and portals after safe destination validation and recovery are proven.
Distance folding extends the same identity, access, bandwidth, and stability systems; it should not become
a separate progression tree. Cross-dimension travel, entities other than players, and chunk loading require
explicit scope decisions rather than being implicit features of the first portal.

Player outcome: connect useful places without bypassing permissions or stranding travelers.

## Optional integration paths

Create, AE2, Mekanism, and IC2 Classic can connect existing factory workflows at the stages where their
adapters make sense. They are parallel options, not successive tiers or required dependencies. Each must
preserve its host mod's resource and network rules. See [Integrations](INTEGRATIONS.md).

## Balancing and delivery rules

- Gate new abilities through understood constraints and small, useful builds rather than bulk resource grind.
- Keep early endpoints useful; avoid replacing every block at each stage.
- Store tunable recipes and values in data or named configuration where appropriate.
- Validate the standalone route before adding optional recipes or integration-specific rewards.
- Define costs, failure behavior, and recovery alongside each new transport or spatial ability.
- Deliver actual 32×32 Minecraft textures and complete model assets with every new visible block or item,
  following [Art Style](ART_STYLE.md).

The [Roadmap](ROADMAP.md) defines implementation slices. Stage order describes the intended learning path,
not a commitment to fixed tiers, recipe quantities, or playtime.
