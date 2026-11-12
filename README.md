# Peterwolf's Groundworks: Concrete

Fabric 26.3 / Java 25 / Groundworks addon.

## Stage 3: stationary concrete mixer

This release adds the first working production machine to the existing finite-volume fresh-concrete and curing engine.

### In-game operation

1. Craft and place **Stationary Concrete Mixer** (iron, hopper, furnace).
2. Right-click the mixer while holding ingredients to load its hopper.
3. Sneak + right-click with an **empty hand** to cycle B10, B20, B30.
4. Right-click with an empty hand to start mixing when enough ingredients are available.
5. Wait **200 game ticks** (nominally 10 seconds at 20 TPS). The drum has a four-frame rotation animation.
6. Right-click with an empty hand after mixing to discharge concrete into the block directly in front of the mixer, in the direction the outlet faces.
7. The fluid flows down and sideways, fills enclosed formwork and hardens after **24,000 ticks**.

The mixer retains unconsumed input amounts and cannot duplicate concrete if the outlet is blocked. It keeps produced concrete in its output buffer and removes only the amount accepted by the world.

### Example B10 batch: one nominal cubic metre (512 units)

- 8 cement bags (25 kg each).
- 1 vanilla sand block (loaded as 750 kg).
- 1 vanilla gravel block (loaded as 1,150 kg).
- 1 water bucket (1,000 L; 150 L consumed, 850 L remains for later batches).

B20 needs 290 kg cement, 650 kg sand, 1200 kg gravel, 165 L water; B30 needs 390 kg cement, 560 kg sand, 1150 kg gravel, 175 L water. Raw input containers accept exact measured mass, and leftover ingredients remain available for subsequent batches.

Additional gameplay input mappings:

| Input item | Mixer storage added |
|---|---:|
| Cement bag | 25 kg cement |
| Sand block | 750 kg sand |
| Gravel block | 1,150 kg gravel |
| Water bucket | 1,000 L water; empty bucket returned in Survival |
| B10/B20/B30 dry mix item | 1/8 m3 of nominal dry ingredients, only when that grade is selected |

The cement bag has a simple gameplay crafting recipe (calcite, clay, coal), not an industrial simulation of cement production. Sand and gravel currently enter as vanilla blocks rather than direct Groundworks granular-container transfers.

### Working mechanics and persistence

- One active batch per machine, maximum 512 microvoxel units.
- Mixing time and materials are stored in the block entity and survive world reloads.
- Server-side ingredient accounting in integer grams and millilitres.
- The output uses the Stage 2 world-pouring API and respects blocked formwork/walls.
- After a partial discharge, the machine retains the exact remainder.
- Visual four-frame drum rotation encoded as block-state models.
- Concrete grade selection, ingredient inventory and status communicated by in-game overlay text.
- Reusable formwork and B10/B20/B30 concrete solidification from Stage 2.
- JUnit tests for input accounting, each grade, persistence restoration, mixing and transfer boundaries.

### Known limitations

- The mixer is a single-block low-poly model, not yet a detailed multiblock machine.
- Drum animation is four model frames, not a skeletal or physically rotating cylinder.
- No mixer-machine inventory GUI yet; use item interactions and overlay messages.
- The world pours at a block position in front of the machine; a hose attachment is deferred.
- The raw ingredient inventory is not currently returned when a stocked machine is broken. Do not break loaded machines.
- Input ratios approximate gameplay material accounting, not certified concrete design.
- Tests are included but cannot be claimed passing until a Minecraft 26.3 / Java 25 Gradle build executes.
- Stage 4 will implement the road mixer truck with a full rotating drum and chute.

## Admin commands

```
/groundworks concrete recipe b10
/groundworks concrete recipe b20
/groundworks concrete recipe b30
/groundworks concrete preview b30 512
/groundworks concrete pour b20 512 100 70 100
/groundworks concrete inspect
/groundworks concrete stats
```

The administrator `pour` command creates test concrete without consuming items. A stationary mixer consumes its ingredients and delivers material through the world API.

## Build

```bash
./gradlew clean test build
```

Required: Java 25, Fabric 26.3, matching Fabric API and Peterwolf's Groundworks.

## Upcoming development

- Stage 4: concrete mixer truck (rotating drum, realistic vehicle model, road transport).
- Stage 5: drum reversal, articulated chute, realistic concrete unloading.
- Stage 6: heavy concrete pump truck, stabilizers and segmented hydraulic boom.
- Stage 7: remote boom and pumping controls.
- Stage 8+: Groundworks granular transfers, lighting, sound and vehicle polish.

## Licence

All Rights Reserved until the project owner selects an explicit licence.
