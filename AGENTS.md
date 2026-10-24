# Convergence Development Rules

## Project
- Minecraft: 1.19.2
- Mod loader: Minecraft Forge
- Java: 17
- Mod ID: `convergence`
- Base package: `io.github.kebab_hub.convergence`

## Product direction
Convergence is a very large techno-magical progression mod designed for long-form playthroughs.
It should combine mechanical engineering, electricity, chemistry, nuclear engineering, aether technology,
automation, computation, fusion, quantum engineering, dimensional engineering, stellar engineering,
antimatter, and reality engineering.

The mod must remain playable by itself. Integrations with Create, Applied Energistics 2, IC2 Classic,
JEI, Botania, Ars Nouveau, and other mods must be optional compatibility layers rather than hard core dependencies
unless a future design document explicitly changes this rule.

## Architecture
- Never reference optional-mod classes from core/common code.
- Put optional integrations under `compat/<modid>/`.
- Keep client-only code isolated from dedicated-server code.
- Prefer Forge registries and DeferredRegister.
- Prefer data-driven recipes, tags, loot, worldgen, and material definitions.
- Avoid giant switch statements for materials, machine tiers, recipes, and integrations.
- Design APIs before duplicating behavior.
- Machine logic should be server-authoritative.
- All machine inventories and fluids must be automation-friendly.
- Plan every mid/late-game machine for AE2 autocrafting compatibility.
- Avoid hidden magic numbers; put progression values in named constants/config/data where appropriate.
- Do not introduce a new external dependency unless it solves a concrete problem and is documented.

## Gameplay
- Difficulty should come primarily from production chains, infrastructure, process conditions, and automation,
  not from requiring absurd amounts of one basic item.
- Old infrastructure should remain useful in later tiers.
- New tiers should compose previous systems instead of replacing them.
- Cross-mod recipes must not make the standalone progression impossible.
- Avoid trivial loops that generate infinite matter, energy, fluids, or currencies.

## Engineering workflow
- Make changes in focused branches/commits.
- Run `gradlew build` after meaningful implementation work when a Gradle wrapper is available.
- Run dedicated-server validation for code that may execute server-side.
- Add or update tests where practical.
- Update docs when architecture or progression changes.
- Do not silently change the supported Minecraft/Forge/Java versions.
- Before large refactors, inspect the relevant code paths and summarize the intended change.
