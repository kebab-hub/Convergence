# Architecture

## Goals
The codebase must stay maintainable while the content count grows into the hundreds of machines and materials.

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
