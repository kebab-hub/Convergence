# Subspace Engineering

Subspace Engineering is a compact systems mod for **Minecraft 1.19.2, Forge, and Java 17**.
Discover and engineer an invisible subspace layer to connect remote blocks, transport capabilities,
create pocket spaces, and fold distance.

## Status

**Foundation and visual asset pipeline. No subspace mechanics are implemented.** This repository replaces the former
Convergence project; its crusher, progression, assets, and integration code have been retired.
The foundation includes typed registries, a creative tab, language data generation, the Gradle wrapper,
and CI. One decorative block, `subspace_test_casing`, demonstrates finished 32×32 textures, a recessed
model, and an animated emissive crystal. A command-testable spark demonstrates particle registration.
There are no machines, recipes, block entities, menus, automatic particle emitters, or subspace systems.

Planned mechanics:

1. **Subspace Interfaces** — configurable endpoints for remote capability access.
2. **Subspace Networks** — shared addressing, routing, and access rules.
3. **Bandwidth / Stability** — visible limits and predictable operating conditions.
4. **Pocket Spaces** — bounded spaces with persistent identity and safe entry/return.
5. **Spatial Folding / Portals** — controlled links that shorten travel.
6. **Create integration** — optional mechanical and factory interoperability.
7. **AE2 integration** — optional storage and automation interoperability.
8. **Mekanism integration** — optional resource and machine interoperability.
9. **IC2 Classic integration** — optional electrical interoperability.

Implementation will favor reusable systems and capability adapters over bespoke machine classes.
Integrations are optional and currently have no dependencies in the build. Each future visible block
or item must arrive with its actual 32×32 Minecraft textures and models in the same change.

## Documentation

- [Vision](docs/VISION.md)
- [Progression](docs/PROGRESSION.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Integrations](docs/INTEGRATIONS.md)
- [Roadmap](docs/ROADMAP.md)
- [Art style and asset delivery](docs/ART_STYLE.md)
- [Asset pipeline and validation](docs/ASSET_PIPELINE.md)
- [Asset sources and tab-atlas generation](docs/ASSET_SOURCES.md)
- [Agent rules](AGENTS.md)

## Development baseline

| Setting | Value |
| --- | --- |
| Minecraft | 1.19.2 |
| Forge | 43.5.2 |
| Java | 17 (64-bit JDK) |
| Mod ID / asset namespace | `subspace` |
| Base package / Maven group | `io.github.kebab_hub.subspace` |
| Gradle / ForgeGradle | 8.8 / 6.0.42 |

## Build and run

Set `JAVA_HOME` to a 64-bit Java 17 JDK. Run commands from the directory containing `build.gradle`.
The wrapper downloads Gradle and Forge dependencies on first use.

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runData
./gradlew runClient
```

Use `./gradlew.bat` on Windows PowerShell. The jar is written to `build/libs/subspace-<version>.jar`.
`runData` generates English names into `src/generated/resources`; generated resources
must be committed. Run data generation before a separate `build` when changing providers so the jar
includes the updated output. GameTests live in `src/gametest` and are excluded from the shipped jar.
`runClient` requires a graphical desktop. `runServer` uses `run-server/` and requires the operator to
accept Minecraft's EULA before hosting. GameTests use a separate `run-gametest/` directory.

Use a new development world for this restart. There is no migration of Convergence registry IDs or saves.

`build` includes `validateAssets`, which checks local model/texture references and animation/particle assets
without launching Minecraft. See the asset pipeline guide for optional Python authoring tools.

## Manual visual check

Install `subspace-0.0.3.jar` in Minecraft 1.19.2 / Forge 43.5.2 using Java 17. Get the casing from the
Subspace Engineering tab or `/give @s subspace:subspace_test_casing`. Check its four orientations, model
depth, slow crystal pulse, nighttime glow, item model, and pickaxe drop. Test the reference spark with
`/particle subspace:subspace_spark ~ ~1 ~ 0.2 0.2 0.2 0.02 20 force`. In-game testing is left to the owner.
