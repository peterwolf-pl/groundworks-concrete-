package com.piotrek.groundworksconcrete.vehicle;

/**
 * Rear-mounted two-axis discharge chute. Degrees are authoritative and bounded.
 * Geometry is expressed in Minecraft blocks independently of Minecraft APIs,
 * so aim calculations can be tested without launching a game client.
 */
public final class MixerTruckChute {
    public static final float MIN_YAW = -65.0F;
    public static final float MAX_YAW = 65.0F;
    public static final float MIN_TILT = 10.0F;
    public static final float MAX_TILT = 55.0F;
    public static final float MOVEMENT_DEGREES_PER_TICK = 1.4F;
    public static final double REAR_PIVOT_OFFSET = 4.0D;
    public static final double CHUTE_LENGTH = 2.3D;
    public static final double PIVOT_HEIGHT = 1.95D;

    public record Point(double x, double y, double z) {}

    private float yawDegrees;
    private float tiltDegrees = 25.0F;

    public float yawDegrees() { return yawDegrees; }
    public float tiltDegrees() { return tiltDegrees; }

    /** Values are clamped in the server controller and during world loading. */
    public void setAngles(float yaw, float tilt) {
        if (!Float.isFinite(yaw) || !Float.isFinite(tilt)) {
            throw new IllegalArgumentException("Non-finite chute angle");
        }
        yawDegrees = Math.clamp(yaw, MIN_YAW, MAX_YAW);
        tiltDegrees = Math.clamp(tilt, MIN_TILT, MAX_TILT);
    }

    public void step(float yawInput, float tiltInput) {
        if (!Float.isFinite(yawInput) || !Float.isFinite(tiltInput)) return;
        setAngles(
                yawDegrees + Math.clamp(yawInput, -1.0F, 1.0F) * MOVEMENT_DEGREES_PER_TICK,
                tiltDegrees + Math.clamp(tiltInput, -1.0F, 1.0F) * MOVEMENT_DEGREES_PER_TICK
        );
    }

    /**
     * World-space endpoint for a truck facing Minecraft's +Z direction at
     * truckYawDegrees=0. Positive chute yaw aims to vehicle's right.
     * The rear pivot is four blocks behind the truck center.
     */
    public Point outlet(double truckX, double truckY, double truckZ, float truckYawDegrees) {
        double yaw = Math.toRadians(truckYawDegrees);
        double horizontalAim = Math.toRadians(yawDegrees);
        double tilt = Math.toRadians(tiltDegrees);
        double forwardX = -Math.sin(yaw), forwardZ = Math.cos(yaw);
        double rightX = Math.cos(yaw), rightZ = Math.sin(yaw);
        double horizontalLength = CHUTE_LENGTH * Math.cos(tilt);
        double back = REAR_PIVOT_OFFSET + horizontalLength * Math.cos(horizontalAim);
        double right = horizontalLength * Math.sin(horizontalAim);

        return new Point(
                truckX - forwardX * back + rightX * right,
                truckY + PIVOT_HEIGHT - CHUTE_LENGTH * Math.sin(tilt),
                truckZ - forwardZ * back + rightZ * right
        );
    }
}
