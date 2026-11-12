package com.piotrek.groundworksconcrete.vehicle;

/** Rotating barrel and reversible internal auger, with no magic discharge. */
public final class MixerTruckDrum {
    public static final float DEFAULT_RPM = 2.0F;
    public static final float MAX_RPM = 12.0F;

    public enum Direction { MIX, DISCHARGE }

    private Direction direction = Direction.MIX;
    private boolean rotating = true;
    private float rpm = DEFAULT_RPM;
    private float angleDegrees;

    public void toggle() { rotating = !rotating; }
    public boolean rotating() { return rotating; }
    public float rpm() { return rpm; }
    public float angleDegrees() { return angleDegrees; }
    public Direction direction() { return direction; }
    public boolean isDischarging() { return rotating && rpm > 0 && direction == Direction.DISCHARGE; }

    public void reverseDirection() {
        direction = direction == Direction.MIX ? Direction.DISCHARGE : Direction.MIX;
    }

    public void setDirection(Direction direction) {
        this.direction = java.util.Objects.requireNonNull(direction);
    }

    public void setRpm(float rpm) {
        if (!Float.isFinite(rpm) || rpm < 0 || rpm > MAX_RPM) {
            throw new IllegalArgumentException("RPM must be between 0 and " + MAX_RPM);
        }
        this.rpm = rpm;
    }

    public float angularVelocityDegreesPerTick() {
        if (!rotating || rpm <= 0) return 0F;
        float directionSign = direction == Direction.MIX ? 1F : -1F;
        return directionSign * rpm * 360F / 1200F;
    }

    public void tick() {
        angleDegrees = (angleDegrees + angularVelocityDegreesPerTick() + 360F) % 360F;
    }

    /** For old Stage 4 saves and tests. */
    public void restore(boolean rotating, float rpm, float angleDegrees) {
        restore(rotating, rpm, angleDegrees, Direction.MIX);
    }

    public void restore(boolean rotating, float rpm, float angleDegrees, Direction direction) {
        setRpm(rpm);
        this.rotating = rotating;
        this.direction = java.util.Objects.requireNonNull(direction);
        this.angleDegrees = Float.isFinite(angleDegrees)
                ? (angleDegrees % 360F + 360F) % 360F : 0F;
    }
}
