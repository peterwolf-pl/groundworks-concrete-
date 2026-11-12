# Peterwolf's Groundworks: Concrete

Fabric addon for Minecraft 26.3, Java 25, Fabric Loader 0.19.5,
matching Fabric API and the Peterwolf's Groundworks base mod.

## Stage 6 - mobile concrete pump truck

Stage 6 introduces an independent driveable concrete-pump vehicle inspired by
the user's photos of the Everdigm truck. It does not replace the mixer truck
from Stage 5.

### Functionality

- Four-axle heavy-duty 8x4 truck with two front steering axles.
- Original low-poly white cab, dark windows, pump mechanism and teal boom.
- Five independently articulated boom sections, 10 + 9 + 9 + 8 + 8 blocks.
- 44-block total theoretical boom length, plus truck mounting height.
- Smooth, limited-speed servo movements: 0.6 degrees/tick per articulation,
  turntable slew 0.42 degrees/tick.
- Separate slewing turntable with a -180 to +180 degree range.
- Per-joint target angles with clamping and server-side position checks.
- World collision checks along every boom segment at <=0.6 block intervals;
  the moving boom halts instead of continuing through buildings.
- Four independently animated hydraulic outriggers. Locking requires all
  four deployed feet to have support in loaded chunks.
- Outriggers cannot start deploying at driving speed, and the truck
  cannot be driven with partially or fully deployed stabilizers.
- Retraction requires the boom to be folded into its travel state.
- A vehicle's boom will not move if any outrigger loses support.
- Automated, hydraulically paced return-to-stow targets, not teleportation.
- Front/back/right/left support jacks, pads, suspension and four wheel axles.
- Distinct model/renderer and inventory icon; original crafting recipe.
- Real in-cab keys and user HUD, server-authoritative multiplayer packets.
- Visual headlights and amber beacons, horn, five articulations and four legs.
- Receiving hopper with 2 cubic metres (1024 finite-volume units),
  grade/ingredient storage and save/load persistence for future Stage 7 pumping.
- Debug commands for support, joints, turntable, stow and hopper filling.
- No cargo duplication across partial fills, incompatible grades, or save/load.
- Tests for boom geometry, controls, support locking and material conservation.

### Stage 6 scope limitations

- THE TRUCK DOES NOT YET PUMP CONCRETE THROUGH ITS BOOM.
  The hopper is implemented, but flow through its pipes and the terminal
  hose will be part of Stage 7.
- WIRELESS CONTROLLER IS NOT YET IMPLEMENTED. Boom controls currently
  operate from the driver seat, or through administrative commands.
- Hydraulic cylinders have articulated animations but do not simulate
  individual piston forces or real load-bearing stress.
- Boom reach is 44 blocks in unfolded geometry; actual reach depends on
  articulation limits and collision geometry.
- Headlights are emissive visual features and do not illuminate terrain.
- The hose on the final segment is graphical only at this stage.
- Minecraft client/Gradle build and in-game testing have not been confirmed
  until a GitHub Actions runner actually executes the build.
- The earlier stationary mixer and mixing truck still have their own
  gameplay and loading logic; normal mixer-to-pump transfer comes in Stage 7.

### Driver-seat controls

| Key | Action |
|---|---|
| W / S | Drive forward or backward (with all outriggers retracted) |
| A / D | Steer |
| P | Deploy or retract four hydraulic stabilizers |
| J | Select the next hydraulic joint (1 through 5) |
| Up / Down arrows | Raise/lower the selected joint |
| Left / Right arrows | Rotate boom turntable |
| K | Fold boom to transport configuration |
| L | Lights and beacon |
| C | Horn |

Use P to deploy legs, wait until all feet are supported and HUD
indicates HYDRAULICS READY, then move the five joints. After work,
press K, allow the boom to return to stowed position, then retract with P.

### Admin test commands

```mcfunction
/groundworks concrete pump inspect
/groundworks concrete pump deploy
/groundworks concrete pump joint 0 40
/groundworks concrete pump joint 1 130
/groundworks concrete pump joint 2 -110
/groundworks concrete pump joint 3 80
/groundworks concrete pump joint 4 -40
/groundworks concrete pump slew 45
/groundworks concrete pump stow
/groundworks concrete pump retract
/groundworks concrete pump fill b20 1024
```

The joint command uses zero-based IDs (0-4). Each target is physically
approached over time, never applied as an instantaneous rotation.
Commands target the nearest mobile pump truck within 32 blocks.
Fill is an operator-only test that creates fresh concrete in the
hopper; ordinary loading from mixer trucks is planned for Stage 7.

### Earlier stages

Stage 1: B10/B20/B30 recipes and exact integer ingredient accounting.

Stage 2: finite-volume wet concrete that fills formwork and hardens
after 24,000 game ticks.

Stage 3: stationary concrete mixer with working ingredient inputs.

Stage 4: driveable four-axle rotating mixer truck.

Stage 5: reverse drum, articulated chute and real world concrete pouring.

## Build

Install Java 25 and run:

```bash
./gradlew clean test build
```

The JAR must not be considered verified until Gradle/CI has actually
compiled the project and the vehicle has been exercised in Minecraft.

## Next

- Stage 7: paired handheld remote control, full hose targeting,
  pump piston/displacement behavior, concrete from mixer truck to pump
  hopper and through the boom with real inventory deductions.
- Later: Groundworks Dump Truck bulk cement/dry mix, engine sounds,
  dynamic site lights and more detailed vehicle models.

## License

All Rights Reserved until the project owner chooses an explicit license.
