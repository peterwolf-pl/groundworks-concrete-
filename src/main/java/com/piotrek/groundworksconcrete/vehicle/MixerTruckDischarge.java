package com.piotrek.groundworksconcrete.vehicle;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteBatchTank;

import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Transactional metered unloading:
 * 1. Propose a bounded portion of the load without removing it.
 * 2. Offer the portion to the finite-volume world engine.
 * 3. Drain exactly the accepted units, never the rejected volume.
 * A blocked hose cannot destroy, dupe, or spill concrete silently.
 */
public final class MixerTruckDischarge {
    public static final float MAX_PARKED_SPEED = 0.022F;
    private MixerTruckDischarge() {}

    /** Rough gameplay conversion: 2 RPM ~ 4 units/tick, 12 RPM ~ 24 units/tick. */
    public static int unitsPerTick(float rpm) {
        if (!Float.isFinite(rpm) || rpm <= 0) return 0;
        return Math.clamp(Math.round(rpm * 2.0F), 1, 24);
    }

    public static int transfer(ConcreteBatchTank tank,
                               MixerTruckDrum drum,
                               float truckSpeed,
                               ToIntFunction<ConcreteBatch> receiver) {
        Objects.requireNonNull(tank);
        Objects.requireNonNull(drum);
        Objects.requireNonNull(receiver);
        if (!Float.isFinite(truckSpeed) || Math.abs(truckSpeed) > MAX_PARKED_SPEED) return 0;
        if (!drum.isDischarging()) return 0;
        ConcreteBatch snapshot = tank.snapshot();
        if (snapshot == null) return 0;
        int proposed = Math.min(snapshot.volumeUnits(), unitsPerTick(drum.rpm()));
        if (proposed <= 0) return 0;

        ConcreteBatch sample = snapshot.split(proposed).taken();
        int accepted = receiver.applyAsInt(sample);
        if (accepted < 0 || accepted > proposed) {
            throw new IllegalStateException("Concrete receiver accepted invalid quantity: " + accepted);
        }
        if (accepted > 0) tank.drain(accepted);
        return accepted;
    }
}
