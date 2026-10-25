# Subspace Engineering — Vision

Subspace Engineering is a compact systems mod about discovering and engineering an invisible
subspace layer. Players use it to connect remote blocks, transport capabilities, create pocket
spaces, and fold distance. A small set of composable tools should produce many useful factory
layouts and spatial experiments.

The target is **Minecraft 1.19.2, Forge, and Java 17**, with mod ID `subspace` and base package
`io.github.kebab_hub.subspace`. This repository reset establishes the direction and a minimal
development foundation. Every gameplay mechanic below is planned, not implemented.

## Core planned mechanics

| Mechanic | Intended role |
| --- | --- |
| Subspace Interfaces | Attach to ordinary blocks and expose selected capabilities through controlled remote endpoints. |
| Subspace Networks | Link interfaces into persistent, inspectable networks with explicit membership and access rules. |
| Bandwidth / Stability | Limit useful throughput and spatial load; give players readable reasons to improve their infrastructure. |
| Pocket Spaces | Provide bounded, persistent spaces anchored to the world and connected through the same network systems. |
| Spatial Folding / Portals | Connect distant locations through deliberate, safe entry and exit points. |
| Create integration | Connect factory logistics and explore kinetic interaction through an isolated optional adapter. |
| AE2 integration | Connect storage and automation workflows while respecting AE2's own network rules. |
| Mekanism integration | Bridge supported logistics capabilities, with specialized resources handled only by explicit adapters. |
| IC2 Classic integration | Support selected electrical and automation interactions without assuming EU and Forge Energy are interchangeable. |

## Design principles

- **Compact content, broad combinations.** Add a block or item only when it provides a distinct player action.
  Extend shared systems before adding another machine with nearly identical behavior.
- **Capability-based engineering.** Reuse inventory, fluid, energy, and other supported capability contracts.
  Optional adapters extend those contracts where needed; every resource does not need a bespoke machine.
- **Readable constraints.** Show connectivity, available bandwidth, stability, and blocked-transfer reasons.
  Capacity limits should invite engineering decisions rather than unexplained failure.
- **Useful early tools.** The first interfaces remain useful as networks grow and spatial features arrive.
- **Standalone progression.** The base mod must work without any integration mod. Integrations provide useful
  alternatives and deeper interoperability, never a mandatory progression gate.
- **Conservative transport.** Moving a resource must not duplicate it or silently destroy it. Unloaded endpoints,
  severed links, full destinations, and permission changes are normal cases that systems must handle.

## Scope boundaries

The project is not a broad industrial progression overhaul. It does not need an ore-processing tree,
dozens of material tiers, a catalogue of unique machines, or a replacement for each integrated mod.
Exact recipes, capacity values, energy costs, discovery method, and spatial limits remain design decisions
for their own implementation slices.

The visual identity is industrial science fiction: dark frames, copper and brass mechanics, cyan controls,
and violet or magenta subspace energy around luminous crystalline cores. Follow [Art Style](ART_STYLE.md).
Every future visible block or item must arrive with its actual Minecraft 32×32 pixel textures and model
assets in the same task; useful physical depth should come from JSON model geometry.

See [Progression](PROGRESSION.md), [Architecture](ARCHITECTURE.md), [Integrations](INTEGRATIONS.md), and
[Roadmap](ROADMAP.md) for the planned path from this foundation.
