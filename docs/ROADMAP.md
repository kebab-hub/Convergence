# Roadmap

## Milestone 0 — Foundation
Goal: a clean Forge 1.19.2 project that builds and launches.

- [x] Establish project vision and engineering rules.
- [x] Add initial Gradle/Forge project files.
- [x] Add minimal mod entrypoint.
- [x] Add Gradle wrapper from the official Forge MDK.
- [x] Confirm `gradlew build` succeeds on Java 17.
- [x] Confirm `gradlew runClient` starts a Forge development client.
- [x] Confirm dedicated-server startup does not load client-only classes.
- [x] Add basic registration helpers.
- [x] Add GitHub Actions build validation.
- [x] Prepare and run data generation.

Validated on 2026-10-24 with 64-bit Temurin 17, Gradle 8.8, ForgeGradle 6.0.42,
and Forge 43.5.2. `build`, `runData`, and `runGameTestServer` passed; the server smoke
test verifies the initialized mod on `Dist.DEDICATED_SERVER`. `runClient` reached the
Forge main menu with Convergence loaded. CI is configured to repeat build, server tests,
and generated-data checks. ForgeGradle emits Gradle 9 deprecation warnings; this project
intentionally uses the MDK's supported Gradle 8.8 wrapper.

## Milestone 1 — First Vertical Slice
Goal: prove the machine/recipe/data model before scaling content.

Initial chain:
```text
Copper Ore
  → Primitive Crusher
  → Crushed Copper
  → Washer
  → Purified Copper
  → Smelting
  → Copper Ingot
  → Plate / Rod / Wire
```

Deliverables:
- first custom item forms;
- first machine block + block entity;
- menu/screen;
- server-side processing;
- persistence;
- recipe type/serializer;
- hopper/item automation;
- JEI display;
- data generation;
- balancing notes.

## Milestone 2 — Mechanical Progression
- Create integration.
- Mechanical Crusher.
- mechanical processing alternatives.
- initial steelmaking path.

## Milestone 3 — Electrical Progression
- native electrical abstraction.
- Electric Crusher.
- generators/storage/cables or clearly defined compatibility boundary.
- IC2 Classic compatibility prototype.

## Milestone 4 — Automation Backbone
- AE2 integration prototype.
- pattern-driven machine automation.
- machine interfaces.
- batch processing and failure handling.

Later milestones will be split from the full progression document once the foundation proves stable.
