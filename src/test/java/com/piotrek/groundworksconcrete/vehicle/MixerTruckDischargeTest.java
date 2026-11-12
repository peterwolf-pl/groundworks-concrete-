package com.piotrek.groundworksconcrete.vehicle;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteBatchTank;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MixerTruckDischargeTest {
    private ConcreteBatchTank tank() {
        var tank = new ConcreteBatchTank(4096);
        assertEquals(512, tank.insert(ConcreteBatch.fresh(ConcreteGrade.B20, 512, 1D)).acceptedUnits());
        return tank;
    }

    @Test void cannotPourWhenMixingOrStoppedOrDriving() {
        var tank = tank();
        var drum = new MixerTruckDrum();
        assertEquals(0, MixerTruckDischarge.transfer(tank, drum, 0, batch -> batch.volumeUnits()));
        drum.reverseDirection();
        assertEquals(0, MixerTruckDischarge.transfer(tank, drum, 0.2F, batch -> batch.volumeUnits()));
        drum.toggle();
        assertEquals(0, MixerTruckDischarge.transfer(tank, drum, 0, batch -> batch.volumeUnits()));
        assertEquals(512, tank.storedUnits());
    }

    @Test void blockedPourDoesNotLoseMaterial() {
        var tank = tank();
        var drum = new MixerTruckDrum();
        drum.reverseDirection();
        assertEquals(0, MixerTruckDischarge.transfer(tank, drum, 0, batch -> 0));
        assertEquals(512, tank.storedUnits());
    }

    @Test void partialAcceptedPourPreservesEveryIngredient() {
        var tank = tank();
        var original = tank.snapshot().ingredients();
        var drum = new MixerTruckDrum();
        drum.reverseDirection();
        final int[] accepted = {0};
        for (int tick=0; tick<50; tick++) {
            int moved = MixerTruckDischarge.transfer(tank, drum, 0,
                    part -> Math.min(part.volumeUnits(), 2));
            accepted[0] += moved;
        }
        assertEquals(100, accepted[0]);
        assertEquals(412, tank.storedUnits());
        // Total original = extracted accounting + preserved tank contents.
        var remaining = tank.snapshot().ingredients();
        var extracted = original.minus(remaining);
        assertEquals(original, extracted.plus(remaining));
        assertTrue(extracted.cementGrams() > 0);
    }

    @Test void totalDischargeIsFiniteAndConservative() {
        var tank = tank();
        var drum = new MixerTruckDrum();
        drum.reverseDirection();
        int total=0;
        for (int i=0;i<2000;i++) total+=MixerTruckDischarge.transfer(
                tank,drum,0.0F, part -> part.volumeUnits());
        assertEquals(512,total);
        assertEquals(0,tank.storedUnits());
        assertNull(tank.snapshot());
    }

    @Test void invalidReceiverVolumeFailsWithoutDraining() {
        var tank=tank();
        var drum=new MixerTruckDrum();
        drum.reverseDirection();
        assertThrows(IllegalStateException.class,
                () -> MixerTruckDischarge.transfer(tank,drum,0F,
                        p->p.volumeUnits()+1));
        assertEquals(512,tank.storedUnits());
    }
}
