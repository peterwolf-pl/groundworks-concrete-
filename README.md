# Peterwolf's Groundworks: Concrete

Fabric addon for Minecraft **26.3**, Java **25** and the matching Peterwolf's Groundworks mod.

## Stage 7: wireless remote and working concrete boom pumping

The full material delivery chain is connected:

**B10/B20/B30 recipe -> stationary mixer -> 8 m3 road mixer -> 2 m3 pump hopper -> articulated boom hose -> finite-volume concrete -> cured foundations.**

### Stage 7 features

- Real transfer from a **parked road mixer truck** whose chute is aligned over the pump's rear receiving hopper.
- Full and incompatible-grade pump hoppers **refuse** delivery. The mixer does not dump the rejected portion on the ground.
- Concrete pumping from the receiving hopper through the five-section boom, discharging at its calculated terminal hose position.
- Both transfer stages operate on the server thread and debit **only confirmed accepted units**, retaining ingredient amounts in grams and water in millilitres.
- Pumping speed adjustable from **1 to 32 volume units per tick** (512 units = one nominal Minecraft m3).
- Pump output automatically stops on empty hopper, unsupported outriggers, stowed boom, emergency stop, or unloaded/blocked pour destination.
- Falling wet-concrete particles and audio at the hose while material actually exits.
- Handheld wireless remote control, craftable and visible in the creative tab.
- **Right-click pump with remote to pair**. The paired operator UUID is stored in the pump's persistent save and synced to clients.
- **Sneak + right-click pump with remote to unpair**. A different player cannot silently take over another player's pairing.
- Remote operations accepted only for the paired player with the remote in either hand, in the same dimension, within **96 blocks**.
- Operator receives remote HUD with E-STOP state, hopper, flow rate, actual flow, selected joint and distance.
- Remote wireless controls: boom slew, selected joint, outriggers, stow, pump start/stop, rate, emergency stop, reset, and aim at looked-at block.
- Numerical inverse kinematics (IK) attempts to position hose near the selected world target with multiple candidate poses, without bypassing server collision checks during real movement.
- Emergency stop **latches**. Press reset explicitly after all four outriggers become supported; pump stays off after reset.
- A vanished or out-of-range remote signal stops remote-driven pumping after 40 ticks.
- The pump does **not** auto-start after reloading a saved world. E-STOP status, pairing and configured flow rate remain saved.
- Operator test commands for every major action and JUnit source tests for conservation, refusal, control range and target geometry.

### Pair and operate the wireless remote

1. Place a concrete pump truck and park on stable ground.
2. Craft/obtain **Concrete Pump Wireless Remote**.
3. With the remote in hand, right-click the truck to pair your UUID.
4. Press **P** to deploy all four outriggers; wait for **HYDRAULICS READY**.
5. Select a section with **J**; use arrow keys to position the boom. **G** attempts IK targeting of the block you are looking at.
6. Drive a mixer truck near the pump's rear hopper. Reverse the mixer drum with **X**, ensure it is spinning with **R**, and align the discharge chute over the receiving opening. The pump hopper holds 2 m3 while the mixer truck holds 8 m3.
7. Press **V** to turn on the pump. Fresh concrete exits at the hose and flows into the formwork.
8. Adjust output with **O** (faster) and **U** (slower).
9. Press **B** for emergency stop. Press **N** to reset after restoring safe hydraulic support; then press V to start again.
10. Turn pump off, fold the boom with **K**, and retract the outriggers with P.

### Controls when standing with the paired remote

| Key | Function |
| --- | --- |
| J | Select a joint, 1 through 5 |
| Left / Right arrows | Boom slewing |
| Up / Down arrows | Raise or lower selected boom joint |
| G | Aim terminal hose at the block under the crosshair using IK |
| P | Deploy/retract outriggers |
| K | Fold boom into transport pose |
| V | Start/stop pumping |
| O / U | Increase/decrease delivery rate |
| B | Emergency STOP, latched |
| N | Reset emergency STOP (requires supported outriggers) |

The remote works while held in either hand. It does not control movement of the road vehicle, which must be driven from the cab.

### Operator test commands

```mcfunction
/groundworks concrete pump deploy
/groundworks concrete pump joint 0 45
/groundworks concrete pump slew 30
/groundworks concrete pump target 10 75 10
/groundworks concrete pump fill b20 1024
/groundworks concrete pump rate 16
/groundworks concrete pump start
/groundworks concrete pump inspect
/groundworks concrete pump stop
/groundworks concrete pump estop
/groundworks concrete pump reset
/groundworks concrete pump unpair
/groundworks concrete pump stow
/groundworks concrete pump retract
```

Use `pump fill` for temporary admin testing. It creates material without consuming raw ingredients. For survival gameplay, prepare a mix in the stationary mixer, load a road mixer truck, and align its chute with the pump hopper. Existing commands for B10/B20/B30 recipes and vehicle diagnostics remain available.

### Safety and constraints

- Each joint has angle and motion-speed limits. Every actual joint movement is checked for terrain collision.
- Four ground-supported hydraulic legs are required for moving or operating the boom. The emergency stop prevents any pumping until explicitly reset.
- Refused deliveries stay inside the donor vehicle/hopper.
- Client packets never specify the amount or type of concrete to be created.
- Remote ownership and signal range are checked on the authoritative game server.
- IK is a gameplay target helper, not a full hydraulic engineering simulation. Obstructions can prevent reaching a valid target.
- The hose currently follows the rigid terminal boom section; an independently articulated dangling hose is planned for further work.
- Visual headlights/beacons do not provide real dynamic light sources.
- New source tests are included but neither Minecraft 26.3 compilation nor in-game behaviour can be confirmed until CI builds a JAR.

## Building

Use Java 25 and the matching Minecraft/Fabric dependencies.

```bash
./gradlew clean test build
```

## Milestones

1. Material recipes and ingredients.
2. Finite-volume fluid and curing.
3. Stationary concrete mixer.
4. Driveable mixer truck.
5. Reversed truck drum and articulated unloading chute.
6. Five-section hydraulic pump truck and four outriggers.
7. **Paired wireless remote, material transfer and real boom concrete delivery.**

Next work: fix/build validation, connected Groundworks dump-truck cement/dry mix, diesel/hydraulic sound effects and more realistic hose and vehicle renderings.

## License

All Rights Reserved until the repository owner selects an explicit license.

