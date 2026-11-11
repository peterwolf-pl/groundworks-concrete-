package com.piotrek.groundworksconcrete.world;

/** Game-time based curing. One normal Minecraft day = 24,000 ticks. */
public final class ConcreteCuring {
    public static final long DURATION_TICKS = 24_000L;
    public static final long FIRST_COLOR_TICK = 8_000L;
    public static final long SECOND_COLOR_TICK = 16_000L;
    public static final long STOP_FLOW_TICK = 20_000L;

    private ConcreteCuring() {}

    public static long elapsed(long gameTime, long pouredAt) {
        return Math.max(0L, gameTime - pouredAt);
    }

    public static int tone(long elapsed) {
        return elapsed >= SECOND_COLOR_TICK ? 2 : elapsed >= FIRST_COLOR_TICK ? 1 : 0;
    }

    public static boolean mayFlow(long elapsed) {
        return elapsed < STOP_FLOW_TICK;
    }

    public static boolean isHardened(long elapsed) {
        return elapsed >= DURATION_TICKS;
    }

    /** Tick for next color/flow/solidification stage. */
    public static long nextBoundary(long pouredAt, long gameTime) {
        long[] checkpoints = { FIRST_COLOR_TICK, SECOND_COLOR_TICK,
                STOP_FLOW_TICK, DURATION_TICKS };
        for (long checkpoint : checkpoints) {
            long at = pouredAt + checkpoint;
            if (at > gameTime) return at;
        }
        return gameTime;
    }
}
