# Peterwolf's Groundworks: Concrete

Fabric addon for Minecraft **26.3**, Java **25** and Peterwolf's Groundworks.

## Stage 2 implemented

- Three independently registered fresh-concrete blocks: **B10, B20, B30**.
- Gravity-first, laterally spreading, lava-like finite-volume simulation.
- No fire, ignition, lava contact effects or infinite concrete source blocks.
- Exactly **512 microvoxel units = one nominal cubic metre**.
- Each live cell retains grade, amount of water, cement, sand, gravel, and initial pour game tick.
- Vertical downhill transfers up to 128 units per update; horizontal equalization has limited slope/viscosity.
- Craftable formwork blocks stop concrete and drop their item when mined after curing.
- Age-based shade progression at 8,000 and 16,000 ticks.
- Flow stops at 20,000 ticks; concrete hardens at 24,000 elapsed world-game ticks.
- Hardened concrete blocks have three grade-specific hardness values, with partial-height shapes at 1/8-block increments.
- Server-side volume and ingredient accounting: split/recombine retains integer gram/millilitre quantities while fresh.
- Per-dimension saved data using Codec to survive world saves and server restarts.
- Bounded priority-queue scheduler with a 128-cell-per-tick limit; sleeping settled cells awaken at the next curing boundary.
- Dedicated world pouring API for later mixer trucks and pump-hose delivery.
- Read-only inspection, fluid pour, and recipe commands.
- Model variants for 8 visual fluid levels and three shades, using vanilla concrete textures.
- Pure Java tests for gravity, formwork barriers, exact ingredient conservation, and curing boundaries.

### Currently simplified

- The fluid is a custom finite-volume Minecraft block simulation, **not a native Fluid implementation**.
- Cell volume is exact while fresh; hardened partial geometry is approximated to the nearest 1/8 block.
- Wet concrete is non-solid and does not exert realistic pressure on entities.
- Flow uses a simple deterministic viscosity/head heuristic, not computational fluid dynamics.
- Different concrete grades cannot mix into the same cell.
- Concrete hardening is a gameplay simplification (1 Minecraft day is not real concrete's 28-day strength timeline).
- Formwork is a reusable oak-textured solid construction block.
- The first graphical pass reuses vanilla textures; custom wet/glossy PBR materials are deferred.
- Formwork is craftable from sticks and oak planks. Bucket inventories and actual cement/sand/gravel-to-batch crafting are still deferred.

## Recipes per nominal 1 m³

| Grade | Cement kg | Sand kg | Gravel kg | Water L |
|---|---:|---:|---:|---:|
| B10 (C8/10) | 200 | 750 | 1150 | 150 |
| B20 (C16/20) | 290 | 650 | 1200 | 165 |
| B30 (C25/30) | 390 | 560 | 1150 | 175 |

Approximate gameplay recipes, not certified construction mix designs.

## Commands (operator permission)

    /groundworks concrete recipe b10
    /groundworks concrete preview b20 512
    /groundworks concrete pour b10 512
    /groundworks concrete pour b20 1024 ~ ~ ~
    /groundworks concrete pour b30 512 100 70 100
    /groundworks concrete inspect
    /groundworks concrete inspect 100 70 100
    /groundworks concrete stats

`pour` is an **administrator testing command**: it creates fresh concrete at the specified position
without consuming items. A normal pumping machine will call `ConcreteWorldApi.pour` and
deduct only accepted units from its own inventory. In-game `pour` without coordinates targets
the air above the block the player is looking at, or the command source's current position.

### Example foundation test

1. Build a floor, surround a 3x3 foundation pit with a wall of `concrete_formwork` blocks.
2. Stand above it and execute `/groundworks concrete pour b20 4096 <x> <y> <z>` at the empty interior.
3. Watch concrete spread downward and sideways. Use `/groundworks concrete stats`.
4. Let the world run for 24,000 game ticks (not real seconds).
5. The shade gradually lightens and wet cells become mined hardened-concrete blocks.

## Build

    ./gradlew clean test build

Requires Java 25, Fabric Loader 0.19.5, Fabric API 0.160.7+26.3 and the compatible Groundworks mod.

## Stage 3 and subsequent work

- Stage 3: working stationary concrete mixer and ingredients-to-batch crafting.
- Stages 4-5: mixer truck, animated drum and discharge chute.
- Stages 6-7: boom pump, outriggers, hydraulic joints and handheld remote.
- Stages 8-10: material transfer, vehicle lighting, audio and Dump Truck cement integration.

## Licence

All Rights Reserved until the repository owner selects an explicit open-source or source-available licence.
