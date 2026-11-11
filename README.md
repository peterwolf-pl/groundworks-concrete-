# Peterwolf's Groundworks: Concrete

**Stage 1: material recipes and exact batch accounting**

Minecraft **26.3**, Fabric Loader **0.19.5**, Fabric API **0.160.7+26.3**, Java **25**, and Peterwolf's Groundworks.

## Stage 1 implemented

- Independent Fabric mod with a creative inventory tab.
- Cement-bag and B10/B20/B30 dry-mix items (temporary vanilla item graphics).
- Read-only in-game recipe and batch-preview commands.
- B10 (C8/10), B20 (C16/20), B30 (C25/30) nominal recipes.
- Dry/fresh batch classes with adjustable water content and recipe-quality indicator.
- Exact integer ingredient accounting in grams and millilitres.
- Groundworks-compatible volumetric units: 512 units = 1 block = nominal 1 m³.
- Immutable batch splits and capacity-restricted tanks without material duplication.
- JUnit tests and GitHub Actions build.

## Nominal concrete recipes per 1 m³

| Grade | Cement kg | Sand kg | Gravel kg | Water L | W/C |
| --- | ---: | ---: | ---: | ---: | ---: |
| B10 (C8/10) | 200 | 750 | 1150 | 150 | 0.75 |
| B20 (C16/20) | 290 | 650 | 1200 | 165 | 0.57 |
| B30 (C25/30) | 390 | 560 | 1150 | 175 | 0.45 |

These are approximate gameplay proportions, **not engineering mix specifications**.
The B-grade mappings use cube strength. Quality and flowability scores are gameplay approximations.

## Commands

- /groundworks concrete recipe b10
- /groundworks concrete recipe b20
- /groundworks concrete recipe b30
- /groundworks concrete preview b10 512
- /groundworks concrete preview b20 256
- /groundworks concrete preview b30 1024

The preview simulates ingredient amounts; it does not spawn concrete or fill trucks.

## Build

Use Java 25. Run:

    ./gradlew clean build

The mod JAR is generated in build/libs. Place it in mods alongside the matching Fabric API and Groundworks mod.

## Architecture / integration boundary

The existing Groundworks 26.3 material registry currently contains fixed numeric
IDs for dirt, sand, gravel and cobblestone. This module intentionally does not
invent conflicting IDs or insert cement into private Groundworks storage.
Sand and gravel will be mapped to existing Groundworks materials when the public
registration/transfer API is extended.

The in-game bag items are Stage 1 placeholders. They do not yet represent loose
volumetric cement on the terrain, nor do they provide functional crafting.

## Not included in Stage 1

- Real flowing and curing concrete blocks (Stage 2).
- Stationary mixing machine (Stage 3).
- Animated mixer truck and discharge chute (Stages 4-5).
- Pump truck, stabilizers, hydraulic articulated boom and remote control (Stages 6-7).
- Actual transfer, pumping, lighting, sounds and Dump Truck integration (Stages 8-10).

## Licence

Source is published without an open-source licence grant. The mod metadata marks
it All Rights Reserved until the owner selects a licence.
