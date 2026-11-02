# Subspace Interface item links

`subspace:subspace_interface` exposes a guarded Forge `ITEM_HANDLER` view of the inventory attached to
one directly paired interface. Hoppers and capability-based pipes can insert/extract through it. There
is no automatic pumping, internal buffer, routing, fluid forwarding, energy forwarding, or mod-specific API.

## Configure a direct A ↔ B pair

1. Place two interfaces. Choose one face on **each** as its local inventory port. Place an inventory
   directly against that face (you can attach it after selecting the faces).
2. Clear the Phase Linker's old selection by sneak-right-clicking air. Select interface A's inventory-port
   face, then interface B's inventory-port face. The two positions must be different; dimensions may differ.
3. Sneak-use that linker on **both** selected interfaces. Each visit changes only the local interface and
   checks local build permission, world interaction permission, distance and spectator state. A partner
   must point back before either end serves items. Selection A/B is retained on the tool.
4. Attach hoppers/pipes to any of the **other five faces**. A accesses the inventory next to B; B accesses
   the inventory next to A. The port face itself and unsided (`null`) queries do not expose the proxy.
5. Right-click an interface for its target, state and transfer limit. Empty-hand sneak-use clears it.

For example, select the **bottom** of both interfaces and place a chest below each. A hopper above A,
pointing down into A, inserts into B's chest. The remote chest is queried on its **top** face. Selecting
a side face instead queries the adjacent inventory's opposite face, preserving sided furnace/machine
rules. The block's horizontal model facing is cosmetic and does not change the chosen port.

The single-end shortcut also works: select only B's port as linker A, apply to interface A, then select
only A's port and apply to B. With a complete A/B selection, applying it to an unrelated third interface
is rejected. Neither configuration action edits a remote block. Existing version-1 direct-inventory
addresses remain saved but now report that the target must be an interface; re-pair them explicitly.
These are location-based links, without ownership, claims integration, or block-instance identity.
Normal interaction permission is checked at each local configuration; capability providers remain
responsible for their automation/access policy. Anchors and their linked counts are unaffected.

## Limits and conservation

The world server config is `serverconfig/subspace-server.toml`:

```toml
# Inserted + extracted items, shared across both directions and all faces of a pair.
interfaceItemsPerTick = 64
```

Allowed range: 0–4096; zero pauses transfers. The limit counts **items**, not stacks or calls. Each actual
accepted/extracted count charges both endpoints. Simulation neither changes inventory nor spends budget.
Partial acceptance spends only the actual amount. Retargeting, switching faces and same-tick save/load
retain spent allowance; both endpoints replenish on the next overworld game tick. There is no fairness
scheduler, so the first caller may consume the tick's allowance.

Each operation resolves a fresh destination and performs at most one corresponding handler mutation.
Insertion passes a bounded copy, returns the complete unaccepted remainder, and never modifies the caller's
stack. Extraction returns only the stack actually returned by the destination, preserving metadata.
Reads return copies so external callers cannot mutate the remote inventory through a reference. Slot
indices, nonpositive extraction requests, missing targets and exhausted budgets fail without consuming
items. Simulation results are advisory; execution uses the current handler and current allowance.

There is no extraction-then-insertion transaction inside this mod and no in-flight payload to save or
drop. The external hopper/pipe owns its usual source-side accounting. Conservation assumes a destination
that obeys Forge's `IItemHandler` contract. A foreign handler that mutates and then throws, lies about a
remainder or ignores simulation cannot be rolled back safely; mutation errors are propagated, never
converted to fabricated success/failure or retried. No cross-inventory crash-atomicity claim is made.

## Availability, cycles and lifecycle

Resolution visits exactly: source interface → reciprocal peer → inventory adjacent to the peer's saved
port. It uses existing worlds, `getChunkNow`, and `EntityCreationType.CHECK` only. No target chunk is
requested or ticketed. An unloaded source, peer or inventory, missing dimension, out-of-bounds position,
removed block/entity, unpaired link, missing item capability or invalidated handler serves no items.
Addresses remain saved for recovery. World borders and build limits apply to the peer and inventory.

A terminal inventory cannot be another `DirectLinkHost`. The implementation never queries the peer's
item proxy to resolve its own. A thread-local guard covers resolution and **every** proxy method, rejecting
reentrant calls from foreign wrappers without recursing. Self-links and nonreciprocal chains are inactive.

The proxy reacquires the inventory and its **sided** capability on every method; no remote entity,
handler or `LazyOptional` is cached. Retarget, clear, load and capability invalidation revoke both local
`LazyOptional`s and retained raw proxy references. New queries obtain new views. Periodic availability/port
changes also invalidate advertised views, allowing consumers to rediscover slots. Unavailability is enforced
immediately on operations; presentation can lag by up to 20 ticks. Consumers must respect capability
invalidation and reacquire their view. Source references left after block removal/unload cannot transfer.

## Code boundaries and persistence

| Component | Responsibility |
| --- | --- |
| `Endpoint` / `DirectLinkConfig` | Existing strict dimension/position/face persistence and malformed-data retention. |
| `LoadedEndpoint` | Operation-local no-load world/block-entity lookup. |
| `DirectItemLink` | Bounded reciprocal-pair resolution, target-side selection and paired budget charge. |
| `DirectItemHandler` | Revocable sided Forge view; slot checks, defensive copies, operation guard. |
| `ItemTransfer` | Single-handler insertion/extraction accounting, used unchanged by headless conservation tests. |
| `TransferBudget` | Tick allowance retained across reconfiguration and persistence. |
| `ItemCallGuard` | Reentrancy rejection across all interface proxy calls on a thread. |
| `DirectLinkInspector` | Immutable item-connection diagnostics, without retained live objects. |
| `SubspaceInterfaceBlockEntity` | Thin configuration/lifecycle host, budgets and visible feedback. |

The existing `DirectLink: {version: 1, target: {...}}` schema remains unchanged. Both interfaces save the
other interface's position and inventory-port face. `ItemBudgetTick` (long) and `ItemBudgetUsed` (int) save
per-endpoint accounting; availability and item handlers are never persisted. Invalid/future link data is
preserved and inactive until explicitly cleared or replaced. Breaking a block drops the ordinary unbound
item. Its contents live in the destination inventory, so there is no interface inventory to spill.
The pre-existing typed channel descriptors remain reusable; only items are exposed or operated here.

## Visual feedback and assets

A valid remote item inventory lights the existing recessed violet core and cyan indicators and emits
light level 5. Disconnection dims the same detailed model. Successful non-simulated movement emits the
existing violet `subspace_spark` at both ends, at most once per five ticks per block. Simulation, merely
reading inventory and rejected transfers create no transfer particles.

This feature reuses the six finished 32×32 interface textures, two 13-element JSON state models, eight
orientation/state variants, item model and complete spark sprite/particle definition. No new visible
content or placeholder art is registered. Recipe, loot and materials are unchanged. Asset references and
texture dimensions are validated by build; rendering is left for owner testing.

## Validation and owner's in-game checks

Run `./gradlew runData` for changed translations, then `./gradlew clean build`. The build runs all prior
headless checks plus `verifyItemTransfers`: 25,000 deterministic conservation cases, tagged stacks, partial
acceptance, differing simulation/execution outcomes, shared budgets, reload/config changes, zero/negative
requests, reentrancy and exception cleanup. The accounting seam uses value stacks so it runs without game
bootstrap; actual Forge `ItemStack`/world behavior is covered by compiled GameTests and manual checks.

`InterfaceGameTests` covers pair creation, local permission/distance, item metadata, copied reads, shared
limits across faces/directions, same-tick reload, raw-view revocation, invalidation/revival, destination
replacement, furnace sides, all six ports, chains, missing dimensions/chunks and vanilla hopper insertion.
These GameTests are **compiled, not executed** for this task. No Minecraft client or gameplay/GameTest
server is launched. The user instruction overrides AGENTS.md's usual GameTest-server workflow.

Use the current normal **`subspace-0.0.9.jar`** with Minecraft 1.19.2, Forge 43.5.2 and Java 17:

1. Pair two interfaces with bottom ports and chests below. Feed A through a downward hopper above it;
   all items must arrive in B's chest. Add a hopper below A while using another face as the inventory port
   to test extraction. Repeat in the opposite direction and with a Forge item pipe if available.
2. Test furnace top/side/bottom ports: respect input, fuel and output restrictions. Test all six chosen
   inventory-port faces, all four cosmetic model facings, and note the local port exposes no remote view.
3. Transfer named/enchanted/NBT items, partial stacks, unstackable items, into full and partially full
   inventories. Count totals before/after; failed insertion returns all unaccepted items.
4. Set the server limit low (e.g. 2), exercise both directions and multiple faces, then set it to 0.
   Check the combined limit, pause and resume; simulation must not consume the allowance.
5. Unload/revisit either endpoint and the inventory chunk, including across dimensions. Nothing should
   force-load. Save/reload, replace/remove the inventory, break a peer, clear and re-pair, and verify recovery.
6. Try A→B→C or adjacent interface terminals, self-links and malformed saved data. No loop or transfer
   should occur. Old direct-inventory configs must remain inert until explicitly reconfigured.
7. Check bright/dim cores and cyan displays, actual-transfer sparks at both ends, correct models/loot,
   and absence of fluid/energy forwarding. No network, anchor membership or additional feature is added.
