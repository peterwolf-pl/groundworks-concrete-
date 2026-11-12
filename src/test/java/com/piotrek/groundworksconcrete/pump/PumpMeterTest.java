package com.piotrek.groundworksconcrete.pump;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteBatchTank;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PumpMeterTest {
    private static ConcreteBatchTank filled(int units) {
        var tank=new ConcreteBatchTank(1024);
        tank.insert(ConcreteBatch.fresh(ConcreteGrade.B30,units,1.12D));
        return tank;
    }

    @Test void fullTransferPreservesVolumeAndChemistry() {
        var tank=filled(400);
        var initial=tank.snapshot().ingredients();
        final long[] delivered={0,0,0,0};
        int total=0;
        for(int tick=0;tick<100;tick++) {
            total+=PumpMeter.transfer(tank,8,batch->{
                var a=batch.ingredients();
                delivered[0]+=a.cementGrams();
                delivered[1]+=a.sandGrams();
                delivered[2]+=a.gravelGrams();
                delivered[3]+=a.waterMillilitres();
                return batch.volumeUnits();
            });
        }
        assertEquals(400,total);
        assertEquals(0,tank.storedUnits());
        assertNull(tank.snapshot());
        assertArrayEquals(new long[]{initial.cementGrams(),initial.sandGrams(),
                initial.gravelGrams(),initial.waterMillilitres()},delivered);
    }

    @Test void refusalDoesNotDrainHopper() {
        var tank=filled(512);
        var before=tank.snapshot().ingredients();
        assertEquals(0,PumpMeter.transfer(tank,32,batch->0));
        assertEquals(512,tank.storedUnits());
        assertEquals(before,tank.snapshot().ingredients());
    }

    @Test void partialAcceptanceOnlyDeductsConfirmedUnits() {
        var tank=filled(300);
        var initial=tank.snapshot().ingredients();
        assertEquals(2,PumpMeter.transfer(tank,20,part->2));
        assertEquals(298,tank.storedUnits());
        assertEquals(initial.cementGrams()>=tank.snapshot().ingredients().cementGrams(),true);
    }

    @Test void invalidReceiverNeverDrains() {
        var tank=filled(200);
        assertThrows(IllegalStateException.class,()->
                PumpMeter.transfer(tank,20,part->part.volumeUnits()+1));
        assertEquals(200,tank.storedUnits());
    }

    @Test void rateLimitsAreApplied() {
        var tank=filled(200);
        assertEquals(32,PumpMeter.transfer(tank,999,ConcreteBatch::volumeUnits));
        assertEquals(168,tank.storedUnits());
        assertEquals(1,PumpMeter.clampRate(0));
        assertEquals(32,PumpMeter.clampRate(500));
    }
}
