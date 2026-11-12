package com.piotrek.groundworksconcrete.vehicle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MixerTruckDriveTest {
    @Test void forwardReverseBrakeAndSteering() {
        MixerTruckDrive controller = new MixerTruckDrive();
        MixerTruckDrive.Result result = null;
        for (int i=0; i<80; i++) result = controller.step(1, 1, false);
        assertNotNull(result);
        assertTrue(result.speed() > 0F && result.speed() <= MixerTruckDrive.MAX_FORWARD);
        assertTrue(result.steerDegrees() > 0F);
        assertTrue(result.yawDeltaDegrees() > 0F);
        for (int i=0; i<30; i++) result = controller.step(0, 0, true);
        assertEquals(0, result.speed(), 0.0001);
        for (int i=0; i<80; i++) result = controller.step(-1, -1, false);
        assertTrue(result.speed() < 0F);
        assertTrue(result.speed() >= MixerTruckDrive.MAX_REVERSE);
        assertTrue(Float.isFinite(result.wheelDegrees()));
    }

    @Test void invalidInputCannotDriveTruck() {
        MixerTruckDrive controller = new MixerTruckDrive();
        var result = controller.step(Float.NaN, Float.POSITIVE_INFINITY, false);
        assertEquals(0, result.speed(), 0.0001);
        assertEquals(0, result.steerDegrees(), 0.0001);
    }

    @Test void restoreClampsDangerousSpeed() {
        MixerTruckDrive controller = new MixerTruckDrive();
        controller.restore(900, 900, 90);
        assertEquals(MixerTruckDrive.MAX_FORWARD, controller.speed());
        assertEquals(MixerTruckDrive.MAX_STEER_DEGREES, controller.steering());
    }
}
