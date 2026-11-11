package com.piotrek.groundworksconcrete.material;

import java.util.Objects;

/**
 * Immutable batch shared by later mixer, pump and curing systems.
 * One Minecraft full block = 512 Groundworks microvoxel units = nominal 1 m³.
 * Mixing ingredients is a game abstraction, not a claim that volume is additive.
 */
public final class ConcreteBatch {
    public static final int UNITS_PER_CUBIC_METRE = 512;
    public enum Phase { DRY, FRESH }

    private final ConcreteGrade grade;
    private final int volumeUnits;
    private final IngredientAmounts ingredients;
    private final Phase phase;

    public ConcreteBatch(ConcreteGrade grade, int volumeUnits, IngredientAmounts ingredients, Phase phase) {
        this.grade = Objects.requireNonNull(grade, "grade");
        this.ingredients = Objects.requireNonNull(ingredients, "ingredients");
        this.phase = Objects.requireNonNull(phase, "phase");
        if (volumeUnits < 1) throw new IllegalArgumentException("Volume must be positive");
        if (ingredients.cementGrams() <= 0) throw new IllegalArgumentException("Cement is required");
        if (phase == Phase.DRY && ingredients.waterMillilitres() != 0) {
            throw new IllegalArgumentException("Dry mix cannot contain water");
        }
        if (phase == Phase.FRESH && ingredients.waterMillilitres() <= 0) {
            throw new IllegalArgumentException("Fresh concrete needs water");
        }
        this.volumeUnits = volumeUnits;
    }

    public static ConcreteBatch dry(ConcreteGrade grade, int volumeUnits) {
        return of(grade, volumeUnits, Phase.DRY, 0);
    }

    public static ConcreteBatch fresh(ConcreteGrade grade, int volumeUnits, double waterMultiplier) {
        if (!Double.isFinite(waterMultiplier) || waterMultiplier <= 0 || waterMultiplier > 2.5) {
            throw new IllegalArgumentException("Water multiplier must be >0 and <=2.5");
        }
        return of(grade, volumeUnits, Phase.FRESH, waterMultiplier);
    }

    private static ConcreteBatch of(ConcreteGrade grade, int units, Phase phase, double waterMultiplier) {
        if (units <= 0 || units > 512_000) throw new IllegalArgumentException("Invalid volume");
        double scale = (double) units / UNITS_PER_CUBIC_METRE;
        IngredientAmounts amounts = new IngredientAmounts(
                Math.round(grade.cementKg() * 1000.0 * scale),
                Math.round(grade.sandKg() * 1000.0 * scale),
                Math.round(grade.gravelKg() * 1000.0 * scale),
                Math.round(grade.waterLitres() * 1000.0 * scale * waterMultiplier));
        return new ConcreteBatch(grade, units, amounts, phase);
    }

    public ConcreteGrade grade() { return grade; }
    public int volumeUnits() { return volumeUnits; }
    public double volumeCubicMetres() { return (double) volumeUnits / UNITS_PER_CUBIC_METRE; }
    public IngredientAmounts ingredients() { return ingredients; }
    public Phase phase() { return phase; }

    /** 0..1 abstract recipe quality; real compressive strength requires testing. */
    public double recipeQuality() {
        if (phase == Phase.DRY) return 0;
        double wcDeviation = relativeDifference(
                ingredients.waterCementRatio(), grade.nominalWaterCementRatio());
        double sandDeviation = relativeDifference(
                ingredients.sandToCementRatio(), (double) grade.sandKg() / grade.cementKg());
        double gravelDeviation = relativeDifference(
                ingredients.gravelToCementRatio(), (double) grade.gravelKg() / grade.cementKg());
        return Math.max(0, Math.min(1,
                1.0 - 0.5 * wcDeviation - 0.25 * sandDeviation - 0.25 * gravelDeviation));
    }

    /** Approximate dimensionless flowability indicator used by later stages. */
    public double workability() {
        if (phase == Phase.DRY) return 0;
        double normalized = ingredients.waterCementRatio() / grade.nominalWaterCementRatio();
        return Math.max(0, Math.min(1, (normalized - 0.7) / 0.6));
    }

    private static double relativeDifference(double actual, double expected) {
        return Math.abs(actual / expected - 1.0);
    }

    public ConcreteBatch combine(ConcreteBatch other) {
        if (other == null || grade != other.grade || phase != other.phase) {
            throw new IllegalArgumentException("Different concrete grade or phase");
        }
        return new ConcreteBatch(grade, Math.addExact(volumeUnits, other.volumeUnits),
                ingredients.plus(other.ingredients), phase);
    }

    public Split split(int takeUnits) {
        if (takeUnits < 1 || takeUnits > volumeUnits) {
            throw new IllegalArgumentException("Transfer must be between 1 and stored volume");
        }
        if (takeUnits == volumeUnits) return new Split(this, null);
        IngredientAmounts taken = ingredients.portion(takeUnits, volumeUnits);
        return new Split(
                new ConcreteBatch(grade, takeUnits, taken, phase),
                new ConcreteBatch(grade, volumeUnits - takeUnits, ingredients.minus(taken), phase));
    }

    /** Null remainder means all volume was taken. */
    public record Split(ConcreteBatch taken, ConcreteBatch remaining) { }
}
