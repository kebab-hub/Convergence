# Subspace Detector

`subspace:subspace_detector` is a craftable scientific instrument that samples background subspace
activity. It introduces the visual language and a small interaction; it has no research, inventory,
energy system, capability transfer, network membership, portal detection, or world-generation effect.

## Using the instrument

- Right-click to display `Subspace activity: N/100 - Quiet/Steady/Elevated` in the action bar.
  An idle detector activates for 100 game ticks (five seconds at 20 TPS).
- Reading an already active detector does not extend its manual timer or repeat the activation sound.
- Redstone power holds the sensor active. Removing redstone turns it off unless a manual scan is still
  finishing. A manual scan can expire while redstone remains on without turning off the powered sensor.
- Sneak-right-click passes through to normal item use, including placing blocks against the instrument.
- The front faces the player on placement. All four horizontal facings have matching active/inactive models.

The active sensor has bright violet facets, cyan waveforms, a short amethyst chime when activation starts,
occasional violet sparks, and light level 7. Inactive screens show a dim standby line and the sensor is
dark violet. The numeric reading is in the player's action bar; the modeled screens indicate operation,
not a second numeric display. The sound uses Minecraft's existing `block.amethyst_block.chime` event.
Particles reuse the mod's finished `subspace_spark` sprite and client provider.

## Reading and implementation

The server computes a deterministic 0–100 ambient sample from the world seed, dimension ID, block
position, and a 100-tick game-time interval. Readings are repeatable within an interval and can vary by
place, dimension, world, and interval. This is a standalone ambient signal, not a measurement of a hidden
resource deposit or an implemented transport network. Bands are Quiet (0–33), Steady (34–66), and Elevated
(67–100). No reading is stored in item NBT or awarded as progression.

`BackgroundActivity` contains only the pure sampling/classification logic. `SubspaceDetectorBlock`
handles the vanilla interaction and redstone hooks. `facing`, `powered`, and `scanning` block-state
properties synchronize the visuals; active means `powered || scanning`. A manual scan schedules one
expiry tick. There is no block entity, per-tick server loop, custom packet, menu, or client renderer.
Minecraft persists the block state and pending scheduled tick through ordinary saves/unloads.
A scanning state placed at a new location (for example by a piston or structure) receives a fresh
expiry tick, so carrying block state without the old location's timer cannot leave the sensor stuck on.

## Crafting and drops

One detector is crafted in a crafting table:

```text
P G P    P = Phase Alloy Plate (2)
  S      G = colorless glass (1)
I R I    S = Stabilized Phase Crystal (1)
         I = Phase Alloy Ingot (2)
         R = Resonance Coil (1)
```

The recipe uses specific Forge/material tags and unlocks after obtaining a Resonance Coil. The detector
is also available in the creative tab or with `/give @s subspace:subspace_detector`. A pickaxe recovers
one detector; the item always starts idle when placed without redstone. Collision is a normal full cube.

## Assets and validation

Four opaque 32×32 textures are committed under `assets/subspace/textures/block/subspace_detector_*.png`:
active/inactive front, side, and top. Two 18-element JSON models provide the body, copper rails and
fasteners, sensor socket and projecting crystal, screen bezels, inset screens, and header plate.
Only the active crystal and screen faces use Forge emissivity; metal keeps normal shading. The item
inherits the inactive model. Blockstates cover all 16 facing/power/manual-scan combinations.

`python tools/assets/generate_detector.py` reproduces textures, models, blockstates, and loot from the
saved art, using nearest-neighbor conversion and the shared palette. Datagen owns language, recipe,
recipe-book unlock, and the shared pickaxe mining tag for both the casing and detector. Run
`./gradlew runData`, then a separate `./gradlew clean build` (`./gradlew.bat` in PowerShell).

`build` runs asset reference checks and `verifyDetectorSignal`, a pure Java check of sample bounds,
repeatability, timing, dimension/seed variation, and bands. Detector GameTests cover manual expiry,
repeated reads, redstone/manual overlap, placement next to existing power, and relocated scan state. They are compiled by
`build` but were **not run**: this task does not launch a Minecraft client or gameplay server.
Texture inspection and an offline render of the JSON geometry complement the file checks; they do not
replace in-game rendering checks. See [Asset Sources](ASSET_SOURCES.md) for prompts and originals.

## Owner's in-game checks

1. Craft or obtain the detector; check its name, inventory/held model, all four orientations, adjacent
   blocks, and pickaxe drop.
2. Right-click while unpowered. Check the action-bar number/band, activation chime, cyan displays,
   violet sensor/sparks, and glow. Confirm it returns to standby after five seconds. Repeated reads
   during that scan should not extend it or replay the chime.
3. Place it beside an already powered source, then test a lever. Check activation on power and standby
   when power ends. Add/remove power during a manual scan; each activation source should finish independently.
4. Save/reload during a manual scan and with sustained redstone power. Check that scans finish and
   powered instruments remain responsive. Test sneak-placement against the detector and low particle settings.
   Move an active detector with a piston and confirm the moved sensor eventually returns to standby.
