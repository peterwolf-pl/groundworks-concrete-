package com.piotrek.groundworksconcrete.vehicle;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteBatchTank;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Truck-specific capacity and precise partial-load conservation. */
class MixerTruckCargoTest {
    private static final int CAPACITY = 8 * ConcreteBatch.UNITS_PER_CUBIC_METRE;

    @Test void eightCubicMetresIsTheMaximum() {
        ConcreteBatchTank tank = new ConcreteBatchTank(CAPACITY);
        for (int n=0;n<8;n++) {
            var load=tank.insert(ConcreteBatch.fresh(ConcreteGrade.B20,512,1D));
            assertEquals(512,load.acceptedUnits());
            assertEquals(0,load.rejectedUnits());
        }
        assertEquals(4096,tank.storedUnits());
        var overflow=tank.insert(ConcreteBatch.fresh(ConcreteGrade.B20,512,1D));
        assertEquals(0,overflow.acceptedUnits());
        assertEquals(512,overflow.rejectedUnits());
    }

    @Test void aPartlyFullTruckDoesNotDuplicateConcrete() {
        ConcreteBatchTank tank=new ConcreteBatchTank(CAPACITY);
        ConcreteBatch offered=ConcreteBatch.fresh(ConcreteGrade.B30,4096,1.1D);
        var first=tank.insert(ConcreteBatch.fresh(ConcreteGrade.B30,3000,1.1D));
        var next=tank.insert(offered);
        assertEquals(3000,first.acceptedUnits());
        assertEquals(1096,next.acceptedUnits());
        assertEquals(3000,next.rejectedUnits());
        assertEquals(offered.ingredients(),
                next.accepted().ingredients().plus(next.rejected().ingredients()));
        assertEquals(4096,tank.storedUnits());
    }

    @Test void differentGradesNeverMixInTruckTank() {
        ConcreteBatchTank tank=new ConcreteBatchTank(CAPACITY);
        tank.insert(ConcreteBatch.fresh(ConcreteGrade.B10,512,1D));
        var refused=tank.insert(ConcreteBatch.fresh(ConcreteGrade.B30,512,1D));
        assertEquals(0,refused.acceptedUnits());
        assertEquals(512,refused.rejectedUnits());
        assertEquals(512,tank.storedUnits());
    }
}
