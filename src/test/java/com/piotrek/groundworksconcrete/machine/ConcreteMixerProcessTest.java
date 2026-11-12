package com.piotrek.groundworksconcrete.machine;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.material.IngredientAmounts;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConcreteMixerProcessTest {
    @Test void b10RequiresEightCementBagsAndOneSandAndGravelBlock() {
        ConcreteMixerProcess p = new ConcreteMixerProcess();
        for(int i=0;i<8;i++) assertTrue(p.add(ConcreteMixerProcess.CEMENT_BAG));
        assertTrue(p.add(ConcreteMixerProcess.SAND_BLOCK));
        assertTrue(p.add(ConcreteMixerProcess.GRAVEL_BLOCK));
        assertEquals(ConcreteMixerProcess.StartResult.INSUFFICIENT_INGREDIENTS, p.start());
        assertTrue(p.add(ConcreteMixerProcess.WATER_BUCKET));
        assertEquals(ConcreteMixerProcess.StartResult.STARTED,p.start());
        assertEquals(0,p.inventory().cementGrams());
        assertEquals(0,p.inventory().sandGrams());
        assertEquals(0,p.inventory().gravelGrams());
        assertEquals(850_000,p.inventory().waterMillilitres());
        assertEquals(512,p.output().volumeUnits());
        assertFalse(p.ready());
        for(int i=0;i<199;i++)assertFalse(p.tick());
        assertTrue(p.tick());
        assertTrue(p.ready());
        assertEquals(ConcreteMixerProcess.StartResult.OUTPUT_NOT_EMPTY, p.start());
        p.dischargeAccepted(64);
        assertEquals(448,p.output().volumeUnits());
        p.dischargeAccepted(448);
        assertNull(p.output());
    }

    @Test void invalidAndMixedGradesAreRejected() {
        ConcreteMixerProcess p = new ConcreteMixerProcess();
        assertFalse(p.addDryMix(ConcreteGrade.B30));
        assertTrue(p.addDryMix(ConcreteGrade.B10));
        p.cycleGrade();
        assertEquals(ConcreteGrade.B20,p.selectedGrade());
    }

    @Test void everyGradeConsumesExactIngredientsAndSurvivesRestore() {
        for(ConcreteGrade grade:ConcreteGrade.values()) {
            ConcreteMixerProcess p = new ConcreteMixerProcess();
            assertTrue(p.choose(grade));
            var recipe = ConcreteBatch.fresh(grade,512,1).ingredients();
            assertTrue(p.add(recipe));
            assertEquals(ConcreteMixerProcess.StartResult.STARTED, p.start());
            assertEquals(IngredientAmounts.empty(),p.inventory());
            var restored = ConcreteMixerProcess.restore(p.selectedGrade(),p.inventory(),
                    p.output(),p.remainingTicks());
            for(int i=0;i<200;i++) restored.tick();
            assertEquals(recipe,restored.output().ingredients());
            restored.dischargeAccepted(512);
            assertNull(restored.output());
        }
    }

    @Test void mixerRejectsOverflowWithoutDuplicatingInputs() {
        ConcreteMixerProcess p = new ConcreteMixerProcess();
        assertFalse(p.add(new IngredientAmounts(4_000_001,0,0,0)));
        assertEquals(IngredientAmounts.empty(),p.inventory());
        assertThrows(IllegalArgumentException.class,()->p.dischargeAccepted(100));
    }

    @Test void dryMixEightItemsPlusWaterCreateB10() {
        ConcreteMixerProcess p = new ConcreteMixerProcess();
        for(int i=0;i<8;i++)assertTrue(p.addDryMix(ConcreteGrade.B10));
        p.add(ConcreteMixerProcess.WATER_BUCKET);
        assertEquals(ConcreteMixerProcess.StartResult.STARTED,p.start());
        assertEquals(ConcreteBatch.fresh(ConcreteGrade.B10,512,1).ingredients(),
                p.output().ingredients());
    }
}
