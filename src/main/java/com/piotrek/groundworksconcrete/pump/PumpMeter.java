package com.piotrek.groundworksconcrete.pump;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteBatchTank;

import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Atomic server-side volume transfer. The receiver accepts a snapshot portion;
 * only its confirmed accepted units are then removed from the supplying tank.
 * The world API and the receiver never create infinite concrete source blocks.
 */
public final class PumpMeter {
    public static final int MIN_RATE = 1;
    public static final int MAX_RATE = 32;
    public static final int DEFAULT_RATE = 8;
    private PumpMeter(){}

    public static int clampRate(int requested){
        return Math.clamp(requested,MIN_RATE,MAX_RATE);
    }

    /**
     * Supply -> receiver, one tick. A failed or partially accepted operation
     * does not waste materials. Must run on one authoritative server thread.
     */
    public static int transfer(ConcreteBatchTank source, int requested,
                               ToIntFunction<ConcreteBatch> receiver){
        Objects.requireNonNull(source);
        Objects.requireNonNull(receiver);
        if(requested <= 0 || source.snapshot() == null)return 0;
        int proposed=Math.min(Math.min(requested,MAX_RATE),source.storedUnits());
        ConcreteBatch offered=source.snapshot().split(proposed).taken();
        int accepted=receiver.applyAsInt(offered);
        if(accepted<0 || accepted>offered.volumeUnits()) {
            throw new IllegalStateException("Receiver returned invalid accepted volume: "+accepted);
        }
        if(accepted>0)source.drain(accepted);
        return accepted;
    }
}
