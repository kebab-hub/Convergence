# Subspace Engineering — Roadmap

This is a new plan. No Subspace Engineering gameplay milestone is complete. The repository reset retains
only generic development infrastructure and the minimal mod bootstrap; historical gameplay validation
does not count as validation of the systems below.

## Foundation — Current reset

- Establish the Subspace Engineering identity: Minecraft 1.19.2, Forge, Java 17, mod ID `subspace`,
  base package `io.github.kebab_hub.subspace`.
- Replace the old gameplay direction with the compact systems design in these documents.
- Retain a minimal build, bootstrap, data-generation hook, and dedicated-server smoke-test scaffold.
- Establish reusable capability-based architecture and the [Art Style](ART_STYLE.md) asset requirement.
- Add no gameplay in this reset. Record actual build and smoke-test results with the change that runs them.

### Bootstrap validation — 2026-10-26

With Java 17, `clean build runData runGameTestServer` passed using the cached dependencies.
The single dedicated-server bootstrap test passed under `subspace`; data generation has no content
providers yet. The packaged jar contains only the three bootstrap classes and metadata, with no
legacy gameplay, optional integrations, or test resources. Documentation links and whitespace checks
passed. No client launch or gameplay validation is claimed for this reset.

## 1. First interface slice — Planned

Deliver one useful connection between two ordinary item inventories. Keep endpoint identity, access,
transfer policy, persistence, and display state separate enough to reuse in later transport types.

Acceptance: links survive reload; transfer honors source and destination rules; full or unloaded endpoints
pause safely; breaking or replacing an endpoint does not duplicate or lose items; users can see connection
status. Include the interface's real textures, blockstate, block/item models, and localization together.

## 2. Networks, bandwidth, and stability — Planned

Generalize the proven link into network membership and routing with a bounded transfer budget. Define a
small stability model and readable feedback. Add fluid and energy capability adapters only after their
transfer and accounting rules are specified; avoid copying the item implementation into bespoke machines.

Acceptance: competing endpoints share capacity predictably; cycles cannot multiply resources; reconnects,
unloads, permission changes, and restarts preserve accounting; overload behavior is understandable and
recoverable. Benchmark bounded work per tick before expanding network scale.

## 3. Pocket spaces — Planned

Define bounded allocation, anchor identity, ownership, persistence, safe arrival, and return paths before
building the first pocket. Reuse network access and load accounting rather than introducing another
independent machine framework.

Acceptance: save/reload, disconnects, anchor removal, unavailable destinations, and failure during entry
have tested recovery paths; players and contents cannot be silently lost or duplicated.

## 4. Spatial folding and portals — Planned

Build a small paired-endpoint travel slice on the established spatial services. Decide explicitly which
entities and dimensions are supported, how costs are reserved, and whether any chunk loading is permitted.

Acceptance: the server checks permissions and destination safety before moving travelers; failed travel
leaves a recoverable state; repeated activation cannot create a transfer loop or duplicate entities.

## 5. Optional tech integrations — Planned

Integration work may run alongside later core slices once the relevant contracts are stable. Each is a
separate, optional adapter with its own narrow acceptance scope:

- **Create:** factory logistics first; assess kinetic interaction separately against the supported API.
- **AE2:** storage and automation interaction that respects AE2 network semantics.
- **Mekanism:** supported logistics first, then explicitly scoped specialized resource adapters.
- **IC2 Classic:** selected automation and electrical interaction with documented EU semantics.

Acceptance for each: standalone launch still works when the mod is absent; the supported 1.19.2 pairing
loads and behaves correctly; transfers preserve resources and respect host-mod limits. Verify actual API
availability before committing to a feature. See [Integrations](INTEGRATIONS.md).

## Gates for every implementation slice

- Keep the deliverable small and playable; extract reusable behavior when the slice demonstrates the need.
- Run a Java 17 build and relevant automated checks; validate server-side changes on a dedicated server.
- Check new visible content in the client with its actual 32×32 textures and complete Minecraft models.
  Placeholder art or a promise to add models later does not complete a block/item task.
- Update design and status docs to distinguish implemented behavior from remaining plans.
- Do not add an integration dependency or a new content family merely to prepare for hypothetical later work.
