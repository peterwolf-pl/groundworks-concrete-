# Peterwolf's Groundworks: Concrete

Fabric addon for Minecraft **26.3**, Java **25**, with Peterwolf's Groundworks.

## Stage 4: four-axle concrete mixer truck

Stage 4 adds a vehicle to Stage 1 recipes, Stage 2 wet concrete physics,
and Stage 3 stationary mixing machinery.

### Implemented

- Placeable and driveable four-axle 8x4 concrete mixer truck with two front steering axles.
- Detailed original Minecraft low-poly cabin, chassis, eight wheels, rear chute and twelve-facet drum.
- Independently animated wheel steering, rolling and continuous rotating concrete drum.
- Teal drum with orange bands, white cabin, dark windows, rear chute and warning beacons.
- Custom-authored 16x16 game texture assets, including a truck inventory icon.
- Server-authoritative WASD movement, interpolation on client and key-bound controls.
- Headlights and rear lamps with emissive renderer, flashing amber beacons.
- Horn using Minecraft's built-in note-block sound.
- Eight cubic metres (4096 Groundworks units) of fresh-concrete storage.
- B10/B20/B30 loads preserved with actual cement, sand, gravel and water masses.
- Loading directly from stationary mixer when the truck is parked near its output.
- Never subtract more mixture from the source than the truck accepts.
- No mixing of different concrete grades in one load; full truck rejects additional material.
- Vehicle persistence (cargo composition, drive state, drum speed/angle, lights).
- No pickup or punch destruction while still carrying concrete.
- HUD displaying volume, grade, drum RPM and controls.
- Administration commands for loading, inspection and RPM adjustment.
- Unit tests for drivetrain, drum, volume conservation, capacity and grade separation.

### Driver controls

| Key | Function |
| --- | --- |
| W/S | Accelerate / reverse |
| A/D | Steer |
| R | Start/stop drum in mixing direction |
| L | Toggle headlights and amber beacons |
| C | Horn |
| Sneak + right-click (empty vehicle) | Pick up empty truck as an item |

The drum defaults to **2 RPM** and is fully animated in the client renderer.
The rotation is in the **mixing direction only** at this stage.

### How to spawn and load a truck

1. Locate the **Concrete Mixer Truck** item in the Groundworks Concrete creative tab.
2. Place it on a clear, flat surface.
3. Drive and park its rear hopper near the stationary concrete mixer's outlet.
4. Produce a B10/B20/B30 batch in the mixer.
5. Right-click the stationary mixer again to send its finished batch into the truck.
6. Drive the loaded truck. The inventory and actual ingredient proportions survive saves.

For quick operator testing without a stationary mixer:

```mcfunction
/groundworks concrete truck fill b10 512
/groundworks concrete truck fill b20 4096
/groundworks concrete truck inspect
/groundworks concrete truck rpm 6
```

The command searches for the **nearest concrete mixer truck within 18 blocks**.
The command respects tank capacity and refuses to blend different grades.
Admin filling creates test material. Normal gameplay loads via the stationary mixer.

### Still available from earlier stages

```mcfunction
/groundworks concrete recipe b20
/groundworks concrete preview b30 512
/groundworks concrete pour b20 512
/groundworks concrete inspect
/groundworks concrete stats
```

### Stage 4 boundaries

- **No reverse drum discharge yet.** Stage 5 introduces auger direction switching and articulated discharge chute.
- The rear chute is modeled but not currently movable or a pouring source.
- No concrete pump truck or hydraulic boom yet.
- No engine audio loop; the horn uses a vanilla sound.
- Headlights are emissive visual features, not true dynamic world lighting.
- No multiplayer integration test has been executed; networking uses server-side authority and driver checks.
- No validated build JAR has been produced because GitHub Actions has not run any build steps.
- Truck body is intentionally low-poly; additional LOD / PBR and diesel audio polish follow later.

## Building

Use Java 25, Minecraft 26.3, Fabric Loader 0.19.5,
Fabric API 0.160.7+26.3, and the matching Groundworks mod.

```bash
./gradlew clean test build
```

## Next milestones

- Stage 5: reverse-rotation discharge, animated adjustable chute, concrete transfer to the world.
- Stage 6: pump truck, hydraulic outriggers and five-section articulated boom.
- Stage 7: remote boom control, hose targeting, pump rate and emergency stop.
- Stage 8+: Groundworks Dump Truck cement/dry-mix support, audio, lighting and visual polish.

## Licence

All Rights Reserved until the project owner selects an explicit licence.
