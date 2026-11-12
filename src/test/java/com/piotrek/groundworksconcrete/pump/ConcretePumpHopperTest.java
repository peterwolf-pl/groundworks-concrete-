package com.piotrek.groundworksconcrete.pump;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteBatchTank;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConcretePumpHopperTest {
    @Test
    void acceptsTwoCubicMetresWithoutCreatingMaterial(){
        ConcreteBatchTank hopper=new ConcreteBatchTank(
                ConcretePumpTruckEntity.HOPPER_CAPACITY);
        var original=ConcreteBatch.fresh(ConcreteGrade.B20,1500,1D);
        var transfer=hopper.insert(original);
        assertEquals(1024,transfer.acceptedUnits());
        assertEquals(476,transfer.rejectedUnits());
        assertEquals(original.ingredients(),transfer.accepted().ingredients()
                .plus(transfer.rejected().ingredients()));
        assertEquals(1024,hopper.storedUnits());
        assertEquals(0,hopper.freeUnits());
    }

    @Test
    void cannotMergeIncompatibleConcrete(){
        ConcreteBatchTank hopper=new ConcreteBatchTank(1024);
        hopper.insert(ConcreteBatch.fresh(ConcreteGrade.B10,512,1D));
        var second=hopper.insert(ConcreteBatch.fresh(ConcreteGrade.B30,256,1D));
        assertEquals(0,second.acceptedUnits());
        assertEquals(256,second.rejectedUnits());
        assertEquals(512,hopper.storedUnits());
    }

    @Test
    void divisionAndDrainConserveAllIngredientMasses(){
        ConcreteBatchTank hopper=new ConcreteBatchTank(1024);
        var original=ConcreteBatch.fresh(ConcreteGrade.B30,850,1.1D);
        hopper.insert(original);
        var removed=hopper.drain(333);
        assertNotNull(removed);
        assertEquals(333,removed.volumeUnits());
        assertEquals(517,hopper.storedUnits());
        assertEquals(original.ingredients(),
                removed.ingredients().plus(hopper.snapshot().ingredients()));
    }
}
