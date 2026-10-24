# Convergence

Convergence is a large-scale techno-magical progression mod for **Minecraft 1.19.2 Forge**.

The project starts with grounded mechanical and electrical engineering and is intended to progress through
industrial chemistry, nuclear technology, Aether engineering, automation, fusion, quantum systems,
dimensional engineering, stellar engineering, antimatter and reality engineering.

## Status

**Milestone 0 — Foundation**

The foundation uses the official Forge 43.5.2 MDK wrapper, typed deferred registration,
data-generation hooks, and Java 17 CI. See the roadmap for verified milestones.

## Documentation

- [Vision](docs/VISION.md)
- [Progression](docs/PROGRESSION.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Integrations](docs/INTEGRATIONS.md)
- [Roadmap](docs/ROADMAP.md)
- [Agent rules](AGENTS.md)

## Development baseline

- Minecraft 1.19.2
- Forge 43.5.2
- Java 17
- Mod ID: `convergence`

## Build and run

Install a **64-bit Java 17 JDK** and point `JAVA_HOME` to it. The wrapper downloads Gradle
8.8 and Forge dependencies on the first build; no global Gradle installation is needed.

```sh
./gradlew build
./gradlew runData
./gradlew runGameTestServer
./gradlew runClient
```

On Windows PowerShell, use `./gradlew.bat` instead. The mod jar is written to `build/libs/`.
Generated resources are committed; run data generation and include its output when changing providers.
`runClient` needs a graphical desktop. `runServer` uses `run-server/` and requires the operator
to accept Minecraft's EULA before hosting a normal world. The automated GameTest server runs
isolated tests in `run-gametest/`; test classes and structures are not shipped in the mod jar.

The wrapper comes from the [official Forge 1.19.2 MDK](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.19.2.html).
See [Forge's Java 17 setup guide](https://docs.minecraftforge.net/en/1.19.2/gettingstarted/).
