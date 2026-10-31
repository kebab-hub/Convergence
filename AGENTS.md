# Subspace Engineering — Development Rules

## Project

- Minecraft: **1.19.2**
- Loader: **Forge** (build pin: 43.5.2)
- Java: **17**
- Mod name: **Subspace Engineering**
- Mod ID / resource namespace: `subspace`
- Base package: `io.github.kebab_hub.subspace`

Subspace Engineering is a compact systems mod about discovering and engineering an invisible subspace
layer that connects remote blocks, transports capabilities, creates pocket spaces, and folds distance.
The planned pillars are Subspace Interfaces, Subspace Networks, Bandwidth / Stability, Pocket Spaces,
Spatial Folding / Portals, and optional Create, AE2, Mekanism, and IC2 Classic integrations.

Convergence's gameplay, industrial eras, material trees, and machine architecture are retired. Only generic
build and validation infrastructure is reusable. This reset adds no gameplay; implement mechanics only in
subsequent tasks that request them. Do not silently change the supported Minecraft/Forge/Java versions.

## Architecture and scope

- **Do not build giant unique Java implementations for every machine.** Compose thin blocks and block
  entities from reusable endpoint, transfer, network, budget, persistence, and access services as needed.
  Avoid copy-pasted machine logic and speculative universal frameworks.
- **Prefer capability-based integration.** Use Forge capabilities for supported resources and focused
  adapters for mod-specific APIs. Never assume different power systems or resource types are interchangeable.
- Keep optional-mod imports and class loading inside guarded `compat/<modid>/` modules. Common code must
  load with all optional mods absent. Verify target-version APIs and document dependencies before adding them.
- Keep client rendering, screens, and input separate from common/server code.
- Use Forge registries and `DeferredRegister`; introduce registry categories only when content needs them.
- Keep topology, transfers, bandwidth, stability, access checks, and spatial operations server authoritative.
  Packets request actions; the server validates them rather than trusting client-supplied outcomes.
- Share transport accounting. Respect sided access, simulation, actual accepted amounts, capability invalidation,
  unloads, and persistence. Prevent duplication, recursive proxy loops, and unbounded work.
- Treat unloaded endpoints as unavailable. Chunk loading must be an explicit, bounded feature.
- Prefer data-driven recipes, tags, loot, models, and named configuration values. Do not recreate a large
  material or energy ecosystem without a concrete need.
- Preserve a standalone progression without integration mods. Integrations extend the same core systems.
- Follow [Architecture](docs/ARCHITECTURE.md), [Integrations](docs/INTEGRATIONS.md),
  [Vision](docs/VISION.md), [Progression](docs/PROGRESSION.md), and [Roadmap](docs/ROADMAP.md).

## Mandatory visual delivery

**Every new visible block or item must ship with its actual Minecraft texture/model assets in the same
task and commit as its registration.** Descriptions, TODOs, missing-texture placeholders, or promises of
later art do not satisfy this rule.

- Follow [Art Style](docs/ART_STYLE.md): 32×32 pixel art, dark gunmetal/near-black frames, copper/brass
  hardware, cyan electronics, vivid violet/magenta subspace energy, and luminous crystalline cores.
- Include original PNG textures and working block/item JSON models. Blocks also require blockstates and
  inventory item models. Include names and relevant loot/data in the same change.
- Use economical JSON model geometry for useful physical depth and readable silhouettes.
- Commit generated resources. Validate resource references and inspect assets in inventory and in-world,
  including supported orientations and states. Report any client inspection that could not run.
- Do not register demonstration content merely to illustrate this reset's art direction.

## Workflow

- Inspect existing changes and preserve unrelated work. Keep changes and commits focused.
- Keep docs aligned with behavior and distinguish planned systems from implemented features.
- Run `./gradlew build` after meaningful implementation/build changes; use `./gradlew.bat` in PowerShell.
- Run `runGameTestServer` for common/server changes and `runData` for data-generation changes.
  Keep tests and their resources out of the shipped jar. Add focused tests for meaningful behavior.
- For capability/spatial work, cover conservation, limits, unload/reload, and safe failure. For integrations,
  validate both presence and absence of the target mod.
- Report actual verification results and gaps. Old Convergence checks do not validate new Subspace features.
