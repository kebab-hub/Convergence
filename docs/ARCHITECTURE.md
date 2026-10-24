# Architecture

## Goals
The codebase must stay maintainable while the content count grows into the hundreds of machines and materials.

## Build and bootstrap

The baseline is Minecraft 1.19.2, Forge 43.5.2, and a 64-bit Java 17 JDK.
The wrapper is copied from the official `forge-1.19.2-43.5.2-mdk.zip`: Gradle 8.8,
with its distribution SHA-256 verified by the wrapper. ForgeGradle is pinned to 6.0.42.
This replaces the initial ForgeGradle 5.1 configuration because the requested Forge MDK
now ships a Gradle 8 / ForgeGradle 6 toolchain. Minecraft, Forge, and Java targets are unchanged.

`Convergence` attaches typed deferred registries and the data-generation listener to the mod bus.
Registry categories are added when content first needs them. `Convergence.id` constructs IDs;
it does not hold gameplay state. Data providers live in `data/`, honor the client/server
generation flags, and write committed output to `src/generated/resources`.

The metadata is expanded from Gradle properties in both development runs and packaged jars.
Minecraft is restricted to exactly 1.19.2 and Forge to the supported 43.x line.
Client, ordinary server, data generation, and GameTest server each use separate run directories.
GameTests live in `src/gametest`, load only in the GameTest run configuration, and are excluded
from the shipped jar. They exercise a real dedicated-server distribution without client classes.

## Proposed package layout

```text
io.github.kebab_hub.convergence
├── Convergence.java
├── api/
├── common/
│   ├── block/
│   ├── blockentity/
│   ├── config/
│   ├── energy/
│   ├── fluid/
│   ├── item/
│   ├── machine/
│   ├── material/
│   ├── menu/
│   ├── network/
│   ├── recipe/
│   ├── research/
│   ├── registry/
│   └── world/
├── client/
│   ├── gui/
│   ├── model/
│   └── renderer/
└── compat/
    ├── ae2/
    ├── create/
    ├── ic2/
    └── jei/
```

## Compatibility boundary
Core code must never load optional-mod classes. Integration bootstrap should first detect whether a mod is loaded,
then initialize its compatibility module. Compatibility packages may depend on their target mod; common packages may not.

## Machine framework direction
A reusable machine framework should eventually model:
- item inputs/outputs;
- fluid/gas inputs/outputs;
- energy inputs;
- process duration;
- process conditions;
- tier/voltage constraints;
- heat/pressure/vacuum/stability;
- recipe lookup;
- persistence;
- sync to client;
- sided automation;
- upgrades;
- multiblock controllers.

Do not implement all of this in the first commit. The first vertical slice should prove the abstractions with one
simple processing chain before generalizing further.

## Data-driven direction
Prefer data files or registries for:
- machine recipes;
- materials and material forms;
- tags;
- research definitions;
- progression metadata;
- integration recipes where practical.

## Networking
Keep gameplay state server authoritative. Packets should request actions or synchronize display state rather than
allowing clients to dictate machine outcomes.
