package com.piotrek.groundworksconcrete.vehicle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MixerTruckChuteTest {
    @Test void chuteAnglesHavePhysicalLimits() {
        MixerTruckChute c = new MixerTruckChute();
        c.setAngles(900, -900);
        assertEquals(MixerTruckChute.MAX_YAW, c.yawDegrees());
        assertEquals(MixerTruckChute.MIN_TILT, c.tiltDegrees());
        c.setAngles(-900, 900);
        assertEquals(MixerTruckChute.MIN_YAW, c.yawDegrees());
        assertEquals(MixerTruckChute.MAX_TILT, c.tiltDegrees());
        assertThrows(IllegalArgumentException.class, () -> c.setAngles(Float.NaN, 20));
    }

    @Test void aimMovesOutletSidewaysAndDownwards() {
        MixerTruckChute c = new MixerTruckChute();
        c.setAngles(0F, 10F);
        var centered = c.outlet(0, 100, 0, 0);
        c.setAngles(45F, 40F);
        var right = c.outlet(0, 100, 0, 0);
        assertTrue(right.x() > centered.x());
        assertTrue(right.y() < centered.y());
        assertTrue(right.z() < -4);
        assertEquals(0.0D, centered.x(), 0.000001D);
    }

    @Test void rotationWithTruckYawKeepsDistanceFromPivotConstant() {
        MixerTruckChute c = new MixerTruckChute();
        c.setAngles(-45F, 25F);
        var a = c.outlet(7, 64, 9, 0);
        var b = c.outlet(7, 64, 9, 90);
        double ad = Math.hypot(a.x()-7, a.z()-9);
        double bd = Math.hypot(b.x()-7, b.z()-9);
        assertEquals(ad, bd, 0.000001D);
        assertEquals(a.y(), b.y(), 0.000001D);
    }
}
