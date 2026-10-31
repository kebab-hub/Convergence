# Subspace Engineering Integrations

## Status and shared contract

Create, Applied Energistics 2 (AE2), Mekanism, and IC2 Classic integrations are planned.
No integration gameplay is implemented in this restart. The standalone mod must load and remain
playable without any of them; none is a required progression gate.

Use standard Forge capabilities when they provide the needed behavior. Add a dedicated adapter
only for a concrete feature that cannot be expressed through that common boundary. All adapters
must use Subspace Engineering's shared endpoint permissions, routing, bandwidth, stability, and
transfer accounting. Do not build a second machine framework for each partner mod.

Before implementation, verify the exact Minecraft 1.19.2 Forge release and its supported APIs.
Record the tested version and dependency scope here. The concepts below are design intentions,
not promises that a particular upstream API supports transparent remote access.

## Create

Role: connect subspace devices to mechanical factories and their logistics.

Planned first step: exchange supported items and fluids through capabilities at fixed endpoints.
Where useful, add optional recipes that let a Create factory manufacture subspace components.

A later kinetic adapter may explore folding rotational connections, but must define speed,
direction, stress, load changes, and disconnection behavior against Create's actual API. Stress
capacity is not a generic stored energy resource. Do not assume an SU-to-FE conversion or support
for moving contraptions; each requires a separate, tested design.

## Applied Energistics 2

Role: make remote endpoints useful to storage and autocrafting infrastructure.

Planned first step: let AE2 automation use the same supported item/fluid interfaces as other
automation. Preserve insertion/extraction rules and report unavailable endpoints consistently.
Test partial acceptance, output backpressure, disconnects, and repeated crafting requests.

A deeper adapter may expose selected network services or coordinate with spatial technology.
It must preserve AE2's own power, channel, security, storage, and crafting rules where applicable.
Subspace links must not silently merge AE2 grids, bypass constraints, or claim compatibility
between AE2 spatial storage and Subspace pocket spaces without an explicit design.

## Mekanism

Role: connect remote industrial resource flows without duplicating processing machinery.

Planned first step: standard item, fluid, and Forge Energy exchange where the target exposes
compatible capabilities. A dedicated adapter may later transport supported chemical resources
using Mekanism's native types and API.

Preserve resource identity, amounts, side configuration, and extraction/insertion constraints.
Do not represent every chemical as a vanilla fluid, assume energy units are interchangeable,
or clone Mekanism's processing systems. Confirm which resource types the chosen 1.19.2 API can
support before adding an adapter and its tests.

## IC2 Classic

Role: connect subspace endpoints to IC2 Classic's electrical infrastructure.

Planned first step: capability-based inventory exchange where supported. An electrical adapter
is a later opt-in feature and must respect IC2 Classic's actual energy-network contract,
including applicable packet, voltage, acceptance, and overload behavior.

EU transport or conversion requires an explicit policy for units, limits, losses, and failure.
Do not assume Forge Energy compatibility or substitute another IC2 variant's API. Keep the
adapter isolated and verify both source and destination accounting under partial acceptance.

## Dependency and loading rules

- Put optional-mod imports and implementation under `compat/<modid>/`; core/common code must
  not reference optional classes in fields, signatures, static initialization, or inheritance.
- Gate adapter loading on the matching mod and supported version. Instantiate only the relevant
  compatibility code, keeping client-only registration out of dedicated-server paths.
- Add API dependencies only when a concrete adapter needs them. Document the reason, version,
  and runtime requirements; do not bundle another mod's implementation into the jar.
- Keep optional recipes and resources conditional on the relevant mods and available registry
  entries. Preserve standalone recipes for every required base feature.
- Prefer shared tags and adapters over duplicate ores, material chains, and machine variants.

## Acceptance criteria for future adapters

Test the base mod with no integrations, then each implemented integration independently and
relevant combinations. Cover dedicated-server startup, unavailable endpoints, capability
invalidation, save/reload, and actual versus simulated transfer amounts. Include failure and
partial-acceptance cases that could otherwise duplicate or lose resources.

Document the supported behaviors and limitations for each adapter. Expose a useful unavailable
state if its dependency disappears; do not corrupt the saved network. Any integration that adds
a visible block or item must deliver its real 32x32 textures and Minecraft model assets in that
same change, following [ART_STYLE.md](ART_STYLE.md).
