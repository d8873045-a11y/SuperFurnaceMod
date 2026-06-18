# SuperFurnaceMod

A Minecraft Forge mod for version **1.12.2** that adds a **Super Furnace** — a block that smelts items **3× faster** than a regular furnace.

## Features

- **Super Furnace block** — visually identical to the vanilla furnace (reuses vanilla textures).
- **3× faster smelting** — cook time is ~66 ticks instead of the vanilla 200 ticks.
- **Full GUI** — identical to the vanilla furnace GUI.
- **Crafting recipe** — 8 Cobblestone (surrounding) + 1 Diamond (center) on a crafting table.
- **Localization** — English (`en_us`) and Russian (`ru_ru`) language support.

## Crafting Recipe

```
C C C
C D C
C C C
```

- `C` = Cobblestone
- `D` = Diamond

## Build Instructions

### Requirements

- Java Development Kit (JDK) 8
- Internet connection (Gradle will download Forge dependencies)

### Steps

```bash
# Clone the repository
git clone https://github.com/d8873045-a11y/SuperFurnaceMod.git
cd SuperFurnaceMod

# Setup the workspace (downloads Minecraft/Forge sources)
./gradlew setupDecompWorkspace

# Build the mod JAR
./gradlew build
```

The compiled `.jar` file will be located at:  
`build/libs/superfurnacemod-1.0.0.jar`

### Running in Development

```bash
# Launch Minecraft client with the mod loaded
./gradlew runClient

# Launch a dedicated server
./gradlew runServer
```

## Mod Details

| Property         | Value                    |
|-----------------|--------------------------|
| Mod ID          | `superfurnacemod`        |
| Version         | `1.0.0`                  |
| Minecraft       | `1.12.2`                 |
| Forge           | `14.23.5.2860` (stable)  |
| Cook time       | 66 ticks (3× faster)     |

## Project Structure

```
SuperFurnaceMod/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/main/
    ├── java/com/example/superfurnacemod/
    │   ├── SuperFurnaceMod.java          ← Main mod class
    │   ├── block/
    │   │   └── BlockSuperFurnace.java    ← Block logic & state toggle
    │   ├── tileentity/
    │   │   └── TileEntitySuperFurnace.java ← Smelting logic (3× speed)
    │   ├── container/
    │   │   └── ContainerSuperFurnace.java  ← Inventory container
    │   ├── gui/
    │   │   ├── GuiSuperFurnace.java      ← Client-side GUI
    │   │   └── GuiHandler.java           ← GUI registration
    │   ├── init/
    │   │   └── ModBlocks.java            ← Block/item registration
    │   └── proxy/
    │       ├── CommonProxy.java          ← Server proxy
    │       └── ClientProxy.java          ← Client proxy (model registration)
    └── resources/assets/superfurnacemod/
        ├── blockstates/                  ← Block state JSON files
        ├── models/block/                 ← Block model JSON files
        ├── models/item/                  ← Item model JSON files
        ├── recipes/                      ← Crafting recipe JSON files
        └── lang/                         ← Language files
```
