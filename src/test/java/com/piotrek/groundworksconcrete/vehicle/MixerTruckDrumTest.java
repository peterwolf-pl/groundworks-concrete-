package com.piotrek.groundworksconcrete.vehicle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MixerTruckDrumTest {
    @Test void rotatesAndStopsWithoutDischarging() {
        MixerTruckDrum drum = new MixerTruckDrum();
        for(int i=0;i<100;i++)drum.tick();
        float before = drum.angleDegrees();
        assertTrue(before > 0F);
        drum.toggle();
        for(int i=0;i<100;i++)drum.tick();
        assertEquals(before,drum.angleDegrees(),0.0001);
        drum.toggle();
        drum.tick();
        assertTrue(drum.angleDegrees() > before);
    }

    @Test void reverseChangesRotationSignAndModePersists() {
        MixerTruckDrum drum = new MixerTruckDrum();
        drum.restore(true, 8.0F, 120F, MixerTruckDrum.Direction.MIX);
        float before = drum.angleDegrees();
        drum.tick();
        float afterMix = drum.angleDegrees();
        assertTrue(afterMix > before);
        drum.reverseDirection();
        assertTrue(drum.isDischarging());
        drum.tick();
        assertEquals(before, drum.angleDegrees(), 0.001);
        assertEquals(MixerTruckDrum.Direction.DISCHARGE, drum.direction());
        drum.restore(true, 5.0F, 70F, MixerTruckDrum.Direction.DISCHARGE);
        assertEquals(MixerTruckDrum.Direction.DISCHARGE, drum.direction());
        drum.toggle();
        assertFalse(drum.isDischarging());
    }

    @Test void rpmLimitAndSaveRestore() {
        MixerTruckDrum drum = new MixerTruckDrum();
        drum.setRpm(9.5F);
        drum.restore(true, 9.5F, 320F);
        drum.tick();
        assertEquals(9.5F, drum.rpm());
        assertThrows(IllegalArgumentException.class, () -> drum.setRpm(-1F));
        assertThrows(IllegalArgumentException.class, () -> drum.setRpm(15F));
    }
}
