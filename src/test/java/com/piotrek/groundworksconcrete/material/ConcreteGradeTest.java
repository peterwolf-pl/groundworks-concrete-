package com.piotrek.groundworksconcrete.material;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConcreteGradeTest {
    @Test void gradeAliases() {
        assertEquals(ConcreteGrade.B10, ConcreteGrade.parse("c8/10"));
        assertEquals(ConcreteGrade.B20, ConcreteGrade.parse("b20"));
        assertEquals(ConcreteGrade.B30, ConcreteGrade.parse("C25/30"));
        assertThrows(IllegalArgumentException.class, () -> ConcreteGrade.parse("B100"));
    }

    @Test void nominalRecipesPerCubicMetre() {
        assertEquals(new IngredientAmounts(200000, 750000, 1150000, 150000),
                ConcreteBatch.fresh(ConcreteGrade.B10, 512, 1.0).ingredients());
        assertEquals(new IngredientAmounts(290000, 650000, 1200000, 165000),
                ConcreteBatch.fresh(ConcreteGrade.B20, 512, 1.0).ingredients());
        assertEquals(new IngredientAmounts(390000, 560000, 1150000, 175000),
                ConcreteBatch.fresh(ConcreteGrade.B30, 512, 1.0).ingredients());
    }

    @Test void nominalWaterCementRatios() {
        assertEquals(.75, ConcreteGrade.B10.nominalWaterCementRatio(), .00001);
        assertEquals(165.0 / 290, ConcreteGrade.B20.nominalWaterCementRatio(), .00001);
        assertEquals(175.0 / 390, ConcreteGrade.B30.nominalWaterCementRatio(), .00001);
    }

    @Test void dryMixHasNoWater() {
        ConcreteBatch dry = ConcreteBatch.dry(ConcreteGrade.B10, 512);
        assertEquals(ConcreteBatch.Phase.DRY, dry.phase());
        assertEquals(0, dry.ingredients().waterMillilitres());
        assertEquals(0, dry.workability());
    }
}
