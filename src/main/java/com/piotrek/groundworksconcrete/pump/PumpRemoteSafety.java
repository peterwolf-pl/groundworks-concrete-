package com.piotrek.groundworksconcrete.pump;

import java.util.Objects;
import java.util.UUID;

/** Authorize a handheld controller by saved operator UUID and world distance. */
public final class PumpRemoteSafety {
    public static final double MAX_RANGE_BLOCKS=96.0D;
    private PumpRemoteSafety(){}

    public static boolean mayOperate(UUID pairedOperator, UUID sender,
                                     boolean remoteHeld, boolean sameDimension,
                                     double distanceSquared) {
        return pairedOperator!=null && Objects.equals(pairedOperator,sender)
                && remoteHeld && sameDimension && Double.isFinite(distanceSquared)
                && distanceSquared<=MAX_RANGE_BLOCKS*MAX_RANGE_BLOCKS;
    }
}
