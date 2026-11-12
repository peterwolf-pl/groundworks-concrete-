package com.piotrek.groundworksconcrete.vehicle;

/**
 * State of the rotating drum. Only the mixing direction is available in Stage 4.
 * Reverse auger/discharge functionality is reserved for Stage 5.
 */
public final class MixerTruckDrum {
    public static final float DEFAULT_RPM = 2.0F;
    public static final float MAX_RPM = 12F;
    private boolean rotating = true;
    private float rpm = DEFAULT_RPM;
    private float angleDegrees;

    public void toggle() { rotating = !rotating; }
    public boolean rotating() { return rotating; }
    public float rpm() { return rpm; }
    public float angleDegrees() { return angleDegrees; }
    public void setRpm(float rpm) {
        if (!Float.isFinite(rpm) || rpm < 0 || rpm > MAX_RPM) {
            throw new IllegalArgumentException("RPM must be between 0 and " + MAX_RPM);
        }
        this.rpm = rpm;
    }

    /** Assumes nominal 20 game ticks/second; stop at zero RPM. */
    public void tick() {
        if (!rotating || rpm <= 0F) return;
        angleDegrees = (angleDegrees + rpm * 360F / 1200F) % 360F;
    }

    public void restore(boolean rotating, float rpm, float angleDegrees) {
        setRpm(rpm);
        this.rotating = rotating;
        this.angleDegrees = Float.isFinite(angleDegrees)
                ? Math.floorMod(Math.round(angleDegrees * 1000F), 360000) / 1000F : 0F;
    }
}
