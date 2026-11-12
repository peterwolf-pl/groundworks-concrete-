# Peterwolf's Groundworks: Concrete

Minecraft **26.3**, Fabric **0.19.5**, Java **25**, Fabric API **0.160.7+26.3**,
with the base Peterwolf's Groundworks mod.

## Stage 5: reversed mixer drum, movable chute and actual concrete discharge

This milestone adds functional pouring to the four-axle truck from Stage 4.
Existing stages remain: B10/B20/B30 recipes and ingredient accounting,
finite-volume fresh concrete/curing, stationary mixer and drivable mixer truck.

### What Stage 5 implements

- Change mixer drum direction from MIX to DISCHARGE.
- Drum rotation reverses visually and physically represents the discharge auger.
- Reverse rotation, an enabled drum, non-zero RPM and an essentially parked vehicle
  are all required before the truck starts releasing concrete.
- Rear U-channel chute has two independently controlled joints:
  - Horizontal swing **-65° to +65°**.
  - Downward tilt **10° to 55°**.
- Chute outlet location follows the truck's yaw and both chute joint angles.
- Fresh concrete exits at the **actual computed chute tip** through
  the existing `ConcreteWorldApi.pour` operation from Stage 2.
- World collisions, enclosure walls and formwork block discharge into occupied cells.
- Exactly the number of accepted finite-volume units is deducted from the drum tank.
- If the chute tip is blocked, or the destination chunk is unloaded, **no cargo is lost**.
- The truck does not silently create concrete or blend incompatible strength grades.
- Discharge rate scales with RPM: **2 units per tick per RPM**, up to 24 units per tick.
  At 2 RPM, a full 4096-unit load takes about 1024 ticks (~51 seconds at 20 TPS)
  if the discharge outlet remains unobstructed.
- Server-authoritative multiplayer controls, displayed in the driver's HUD.
- Visual block particles and intermittent pouring audio while material actually exits.
- New state synchronized and saved: reverse direction, chute yaw/tilt and drum angle.
- Includes tests for reversible drum movement, aim geometry, containment limits,
  blocked destination conservation, partial acceptance and emptying a full tank.
- Fixes Stage 4's missing truck entity/network registration in the mod initializer.

### Driver controls

| Key | Action |
| --- | --- |
| W / S | Drive forward / reverse |
| A / D | Steering |
| R | Start / stop mixer drum |
| X | Reverse drum direction: mix / discharge |
| Left / Right arrows | Swing chute left / right |
| Up / Down arrows | Raise / lower chute tip |
| L | Headlights and amber beacon |
| C | Horn |

All special keys are remappable in Minecraft Controls.

### Example: concrete in formwork

1. Place a mixer truck and build an enclosed, open-top formwork foundation.
2. Load the truck from the Stage 3 stationary mixer, or with an operator command.
3. Park near the foundation and aim the rear chute over its empty interior.
4. Press **R** if the drum is stopped; press **X** to select reverse/discharge.
5. Adjust the aim with the arrow keys. The HUD reports mode, current angles and
   whether concrete actually flows.
6. Concrete falls, spreads and fills the formwork with the Stage 2 simulation.
7. After **24,000 elapsed game ticks**, it becomes hardened B10/B20/B30 concrete.

The truck stops pouring if it starts moving too fast, the tank becomes empty,
the drum stops, the operator selects mixing direction, or the outlet cannot accept concrete.

### Operator/debug commands

```mcfunction
/groundworks concrete truck fill b20 4096
/groundworks concrete truck inspect
/groundworks concrete truck rpm 8
/groundworks concrete truck direction discharge
/groundworks concrete truck direction mix
/groundworks concrete truck chute yaw -30
/groundworks concrete truck chute tilt 45
```

The commands target the nearest mixer truck within 18 blocks.
The admin fill command creates test concrete without consuming raw materials.
Normal gameplay loading uses the Stage 3 stationary mixer.

### Technical boundaries

- The Stage 5 chute is two-axis articulated; it is not yet telescoping or folding
  into multiple nested extensions.
- The discharge rate is a simplified gameplay approximation, not pump/auger CFD.
- The concrete behaves as Stage 2's finite-volume flowing blocks, not vanilla lava.
- The current vehicle renderer uses its low-poly drum mesh and existing texture palette.
- This is the mixer **truck**, not the dedicated boom pump. The multi-jointed pump
  and remote controls are the following milestones.
- Headlights remain visually emissive without dynamic light casting.
- Minecraft/Fabric compilation and in-game regression tests require a working runner.
  A successful JAR is not claimed until those checks pass.

## Build

Run on Java 25:

```bash
./gradlew clean test build
```

Install the produced, successfully verified JAR alongside the matching Fabric API
and Peterwolf's Groundworks version.

## Upcoming

- Stage 6: large truck-mounted concrete pump with stabilizers and a five-joint boom.
- Stage 7: hydraulic remote control, pump rates, hose position and emergency stop.
- Stage 8+: Dump Truck integration for cement/dry mix and audiovisual polish.

## License

All Rights Reserved until a specific project license is selected.
