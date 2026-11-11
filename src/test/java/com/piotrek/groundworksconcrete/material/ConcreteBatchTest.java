package com.piotrek.groundworksconcrete.material;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConcreteBatchTest {
    @Test void preservesEveryIngredientAcrossSplit() {
        ConcreteBatch original = ConcreteBatch.fresh(ConcreteGrade.B30, 777, 1.16);
        ConcreteBatch.Split split = original.split(311);
        assertEquals(original.volumeUnits(),
                split.taken().volumeUnits() + split.remaining().volumeUnits());
        assertEquals(original.ingredients(),
                split.taken().ingredients().plus(split.remaining().ingredients()));
        assertEquals(original.ingredients(),
                split.taken().combine(split.remaining()).ingredients());
    }

    @Test void exactFullTransferHasNoRemainder() {
        ConcreteBatch batch = ConcreteBatch.fresh(ConcreteGrade.B20, 512, 1);
        assertSame(batch, batch.split(512).taken());
        assertNull(batch.split(512).remaining());
    }

    @Test void waterChangesQualityAndWorkability() {
        ConcreteBatch nominal = ConcreteBatch.fresh(ConcreteGrade.B20, 512, 1);
        ConcreteBatch wetter = ConcreteBatch.fresh(ConcreteGrade.B20, 512, 1.5);
        ConcreteBatch drier = ConcreteBatch.fresh(ConcreteGrade.B20, 512, .5);
        assertEquals(1, nominal.recipeQuality(), .00001);
        assertTrue(nominal.recipeQuality() > wetter.recipeQuality());
        assertTrue(wetter.workability() > nominal.workability());
        assertTrue(drier.workability() < nominal.workability());
    }

    @Test void rejectsInvalidMixtures() {
        assertThrows(IllegalArgumentException.class,
                () -> ConcreteBatch.fresh(ConcreteGrade.B20, 512, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ConcreteBatch.fresh(ConcreteGrade.B20, 512, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> ConcreteBatch.dry(ConcreteGrade.B10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ConcreteBatch.fresh(ConcreteGrade.B30, 512, 1).split(513));
    }

    @Test void differentGradesCannotBeCombined() {
        assertThrows(IllegalArgumentException.class,
                () -> ConcreteBatch.fresh(ConcreteGrade.B10, 512, 1)
                        .combine(ConcreteBatch.fresh(ConcreteGrade.B30, 512, 1)));
    }
}
