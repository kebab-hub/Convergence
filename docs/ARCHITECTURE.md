# Subspace Engineering Architecture

## Status and baseline

This restart contains a minimal mod bootstrap and reusable development tooling. All gameplay
systems below are planned; none are implemented by this document. The project targets
Minecraft **1.19.2**, **Forge 43.5.2**, and **Java 17**, with mod ID `subspace` and base package
`io.github.kebab_hub.subspace`.

Retain the generic Gradle wrapper, Forge run configurations, resource processing, data-generation
hooks, and isolated GameTest setup. Register content only when its feature is implemented.
The previous mod's machines, recipes, materials, and progression are not a foundation for this design.

## Composition before machine classes

Subspace Engineering is a small collection of cooperating systems. A visible device configures and
exposes those systems; it should not duplicate routing, transfer, persistence, or budgeting logic.
Prefer focused services, capability adapters, and data definitions over a unique large Java class
for every machine or a universal machine superclass with dozens of unrelated modes.

Build only the abstractions needed by the next working slice. Begin with two interfaces and one
resource type; extract shared behavior as concrete requirements appear. Do not prebuild every
integration, transport type, pocket-space manager, or portal framework.

## Planned responsibilities

| System | Owns | Does not own |
| --- | --- | --- |
| Subspace Interface | Endpoint configuration, sided access, filters, local presentation | A private routing or transfer implementation |
| Subspace Network | Membership, endpoint lookup, permissions, route validity | Other mods' inventories or internal network state |
| Capability transport | Typed adapters, transfer accounting, bounded pending payloads | Implicit conversion between incompatible resource types |
| Bandwidth / Stability | Shared admission budgets, operating limits, recovery state | Independently replenished budgets on every endpoint |
| Pocket Spaces | Space identity, allocation, access, persistence, safe return locations | A replacement machine progression |
| Spatial Folding / Portals | Validated links, traversal, destination checks | Arbitrary client-authorized movement or chunk loading |
| Compatibility adapters | Optional API translation into the shared systems | Separate copies of core gameplay |

Interfaces initially connect remote blocks through their supported capabilities. A link must
preserve the target's sided access rules and resource semantics. Future spatial features should
reuse network identity, permissions, and operating budgets where appropriate without forcing
entity traversal through an item-transfer abstraction.

## Planned code boundaries

Under `io.github.kebab_hub.subspace`, introduce packages only when needed:

- `common/registry`: typed Forge registration; keep content identity separate from behavior.
- `common/interfaceblock`, `common/network`, `common/transport`: endpoint configuration, topology,
  routing, and resource-specific adapters with shared scheduling.
- `common/subspace`: bandwidth and stability policy shared by the network's operations.
- `common/pocket`, `common/folding`: space lifecycle and safe traversal.
- `common/config`, `common/menu`, `common/networking`: tunable limits, interaction, and packets.
- `data`: recipes, tags, loot, models, block states, and translations.
- `client`: screens, renderers, and client-only registration.
- `compat/<modid>`: optional integrations, isolated from common code.
- `api`: only contracts that have an actual external consumer.

Keep the subspace topology separate from packet networking. Keep client classes out of the
dedicated-server class-loading path. Packets request actions; the server validates the sender,
permissions, distance where applicable, endpoint identity, and current operating state.

## Network identity and lifecycle

Use stable network and space identities. An endpoint reference needs a dimension, position,
attachment side, and configuration; never persist a live block entity or capability object.
Ownership and access policy belong to saved network state and must also govern remote access.

Separate persisted membership from the currently available endpoint set. An unloaded chunk,
removed block, invalidated capability, or missing integration makes the affected endpoint
unavailable. Release stale references and reacquire access when the endpoint becomes available.
Do not load arbitrary chunks merely to resolve a route. Any later chunk-loading feature needs
an explicit bounded policy and cost.

Topology changes invalidate routes and must not reset spent bandwidth or copy pending payloads.
Define deterministic merge/split ownership rules before supporting either operation. Keep
searches, work queues, per-tick transfers, and saved pending data bounded. Avoid scanning every
block in every loaded chunk on each tick.

## Capability transport and conservation

Start with standard Forge item capabilities, then extend to fluids and Forge Energy through
typed adapters. A shared scheduler handles admission, routing, and accounting; each adapter
retains its resource's native semantics. Exposing a remote capability is a controlled view of
an endpoint, not ownership of its contents.

The transfer contract must address these cases before shipping:

- Respect sided access, filters, stack/fluid metadata, and the actual amount accepted or extracted.
- Simulation is advisory, not an atomic transaction across two arbitrary handlers. Revalidate at
  execution and account for partial success.
- Materialize only what was actually extracted. Retain any undelivered remainder in bounded,
  persistent custody or return it through a verified path; never silently discard it or credit
  both the source and destination.
- Specify save, reload, break, and unload behavior for pending payloads before asynchronous
  transport is introduced. Do not promise cross-handler crash atomicity without a concrete design.
- Reject self-links and recursive proxy loops. A routing cycle must not multiply resources,
  produce unbounded calls, or charge the same successful operation inconsistently.
- Invalidate exposed capability views with the block entity lifecycle. Do not retain invalid
  handles or let a disconnected endpoint continue transferring.

Use narrowly scoped adapters for external resource types. Incompatible power systems or chemical
types require explicit semantics and balancing decisions; they are not interchangeable numbers.

## Bandwidth and stability

Bandwidth limits how much work a network can admit over time. Stability limits the operating
conditions under which a route or spatial operation is allowed. Both are planned gameplay
constraints, not additional resource inventories created for each device.

Keep cost policies and thresholds in named configuration or data where appropriate. Share
accounting across endpoints so adding interfaces cannot bypass a network limit. Define fairness,
backpressure, and the treatment of failed operations. Show players the limiting condition and
recovery path. Initial failure behavior should pause or refuse work safely rather than destroy
stored resources, spaces, or occupants. Numerical tuning remains undecided.

## Pocket spaces and folding

Pocket-space ownership, bounds, entrances, and return locations must survive saves and restarts.
Decide allocation and reclamation rules before creating dimensions or regions. Removing an
entrance must not orphan stored contents or strand a player; reclamation requires an explicit
safe recovery design.

Folding and portals validate both ends, access permissions, destination availability, safe arrival,
and operating limits before committing movement. Handle cooldowns and loops. Plan disconnects,
restarts, removed anchors, and interrupted traversal, including a safe fallback return. These
systems follow a proven transport core and are not part of the initial bootstrap.

## Data, assets, and validation

Prefer data-driven recipes, tags, loot, and device parameters. Keep save formats versioned when
state appears, and document migration behavior when changing them. Sync only state clients need.

Every new visible block or item must include its actual Minecraft textures and model assets in
the same change, following [ART_STYLE.md](ART_STYLE.md). Include block states, item models, and
other required resource references; use JSON model geometry for useful physical depth. A future
art task or a missing-texture placeholder does not complete a gameplay feature.

Validate each implemented slice with focused tests for conservation, capacity limits, reconnects,
save/reload, sided access, and absent optional mods. Spatial work additionally needs occupied-space
recovery and interrupted-traversal checks. Run the Java 17 build and dedicated-server checks for
runtime changes; visually inspect new content in a client. These are future feature acceptance
criteria, not claims that gameplay tests already exist.
