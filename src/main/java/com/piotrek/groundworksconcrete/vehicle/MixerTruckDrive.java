package com.piotrek.groundworksconcrete.vehicle;

/**
 * Deterministic ground vehicle controller shared by server tick and unit tests.
 * Two steerable front axles, two trailing rear axles, speed in blocks per tick.
 */
public final class MixerTruckDrive {
    public static final float MAX_FORWARD = 0.31F;
    public static final float MAX_REVERSE = -0.16F;
    public static final float ACCELERATION = 0.012F;
    public static final float BRAKE = 0.030F;
    public static final float FRICTION = 0.009F;
    public static final float MAX_STEER_DEGREES = 30.0F;
    private static final double WHEELBASE_BLOCKS = 4.25D;
    private static final double WHEEL_RADIUS_BLOCKS = 0.56D;

    private float speed;
    private float steering;
    private float wheelDegrees;

    public record Result(float speed, float steerDegrees, float wheelDegrees, float yawDeltaDegrees) {}

    public Result step(float throttle, float steer, boolean braking) {
        if (!Float.isFinite(throttle) || !Float.isFinite(steer)) {
            throttle = 0.0F; steer = 0.0F;
        }
        throttle = Math.clamp(throttle, -1.0F, 1.0F);
        steer = Math.clamp(steer, -1.0F, 1.0F);

        if (braking || (throttle * speed < 0.0F)) {
            speed = approach(speed, 0.0F, BRAKE);
        } else if (Math.abs(throttle) > 0.05F) {
            speed = Math.clamp(speed + throttle * ACCELERATION, MAX_REVERSE, MAX_FORWARD);
        } else {
            speed = approach(speed, 0.0F, FRICTION);
        }
        steering = approach(steering, steer * MAX_STEER_DEGREES, 3.0F);
        float yawDelta = (float) Math.toDegrees(speed / WHEELBASE_BLOCKS
                * Math.tan(Math.toRadians(steering)));
        float rollDelta = (float) Math.toDegrees(speed / WHEEL_RADIUS_BLOCKS);
        wheelDegrees = (wheelDegrees + rollDelta) % 360.0F;
        return new Result(speed, steering, wheelDegrees, yawDelta);
    }

    private static float approach(float value, float target, float rate) {
        if (value < target) return Math.min(target, value + rate);
        return Math.max(target, value - rate);
    }

    public float speed() { return speed; }
    public float steering() { return steering; }
    public float wheelDegrees() { return wheelDegrees; }

    public void restore(float speed, float steering, float wheelDegrees) {
        this.speed = Float.isFinite(speed) ? Math.clamp(speed, MAX_REVERSE, MAX_FORWARD) : 0F;
        this.steering = Float.isFinite(steering)
                ? Math.clamp(steering, -MAX_STEER_DEGREES, MAX_STEER_DEGREES) : 0F;
        this.wheelDegrees = Float.isFinite(wheelDegrees) ? wheelDegrees % 360F : 0F;
    }
}
