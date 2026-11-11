package com.piotrek.groundworksconcrete.material;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConcreteBatchTankTest {
    @Test void partialFillAndDrainConserveAmounts() {
        ConcreteBatch incoming = ConcreteBatch.fresh(ConcreteGrade.B10, 512, 1);
        ConcreteBatchTank tank = new ConcreteBatchTank(400);
        var transfer = tank.insert(incoming);
        assertEquals(400, transfer.acceptedUnits());
        assertEquals(112, transfer.rejectedUnits());
        assertEquals(incoming.ingredients(),
                transfer.accepted().ingredients().plus(transfer.rejected().ingredients()));

        ConcreteBatch drained = tank.drain(125);
        assertEquals(275, tank.storedUnits());
        assertEquals(transfer.accepted().ingredients(),
                drained.ingredients().plus(tank.snapshot().ingredients()));
    }

    @Test void cannotOverfillOrMixGrades() {
        ConcreteBatchTank tank = new ConcreteBatchTank(512);
        assertEquals(512, tank.insert(ConcreteBatch.fresh(ConcreteGrade.B20, 512, 1)).acceptedUnits());
        assertEquals(0, tank.freeUnits());
        var refused = tank.insert(ConcreteBatch.fresh(ConcreteGrade.B30, 100, 1));
        assertEquals(0, refused.acceptedUnits());
        assertEquals(100, refused.rejectedUnits());
        assertEquals(512, tank.storedUnits());
    }

    @Test void dryAndWetPhasesAreNotMixed() {
        ConcreteBatchTank tank = new ConcreteBatchTank(1024);
        tank.insert(ConcreteBatch.dry(ConcreteGrade.B10, 512));
        assertEquals(0, tank.insert(ConcreteBatch.fresh(ConcreteGrade.B10, 512, 1)).acceptedUnits());
    }

    @Test void drainingEmptyTankIsSafe() {
        ConcreteBatchTank tank = new ConcreteBatchTank(512);
        assertNull(tank.drain(100));
        assertEquals(0, tank.storedUnits());
    }
}
