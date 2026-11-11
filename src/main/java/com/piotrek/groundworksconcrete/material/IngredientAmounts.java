package com.piotrek.groundworksconcrete.material;

/**
 * Integer ingredient inventory: masses in grams and water in millilitres.
 * Integer storage prevents material appearing or disappearing during transfers.
 */
public record IngredientAmounts(
        long cementGrams, long sandGrams, long gravelGrams, long waterMillilitres
) {
    public IngredientAmounts {
        if (cementGrams < 0 || sandGrams < 0 || gravelGrams < 0 || waterMillilitres < 0) {
            throw new IllegalArgumentException("Negative ingredient amount");
        }
    }

    public static IngredientAmounts empty() {
        return new IngredientAmounts(0, 0, 0, 0);
    }

    public IngredientAmounts plus(IngredientAmounts other) {
        return new IngredientAmounts(
                Math.addExact(cementGrams, other.cementGrams),
                Math.addExact(sandGrams, other.sandGrams),
                Math.addExact(gravelGrams, other.gravelGrams),
                Math.addExact(waterMillilitres, other.waterMillilitres));
    }

    public IngredientAmounts minus(IngredientAmounts other) {
        return new IngredientAmounts(
                cementGrams - other.cementGrams,
                sandGrams - other.sandGrams,
                gravelGrams - other.gravelGrams,
                waterMillilitres - other.waterMillilitres);
    }

    /** Integer proportional extraction. The unsplit remainder retains all rounding residues. */
    public IngredientAmounts portion(int takenUnits, int totalUnits) {
        if (totalUnits <= 0 || takenUnits < 0 || takenUnits > totalUnits) {
            throw new IllegalArgumentException("Invalid transfer volume");
        }
        return new IngredientAmounts(
                Math.multiplyExact(cementGrams, takenUnits) / totalUnits,
                Math.multiplyExact(sandGrams, takenUnits) / totalUnits,
                Math.multiplyExact(gravelGrams, takenUnits) / totalUnits,
                Math.multiplyExact(waterMillilitres, takenUnits) / totalUnits);
    }

    public double waterCementRatio() {
        return cementGrams == 0 ? 0 : (double) waterMillilitres / cementGrams;
    }

    public double sandToCementRatio() {
        return cementGrams == 0 ? 0 : (double) sandGrams / cementGrams;
    }

    public double gravelToCementRatio() {
        return cementGrams == 0 ? 0 : (double) gravelGrams / cementGrams;
    }
}
