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
