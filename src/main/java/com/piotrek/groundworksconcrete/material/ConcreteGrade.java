package com.piotrek.groundworksconcrete.material;

import java.util.Locale;

/**
 * Nominal recipes for one cubic metre. Values are gameplay approximations,
 * not certified structural-concrete mix designs.
 */
public enum ConcreteGrade {
    B10("C8/10", 10, 200, 750, 1150, 150),
    B20("C16/20", 20, 290, 650, 1200, 165),
    B30("C25/30", 30, 390, 560, 1150, 175);

    private final String europeanClass;
    private final int nominalMpa;
    private final int cementKg;
    private final int sandKg;
    private final int gravelKg;
    private final int waterLitres;

    ConcreteGrade(String europeanClass, int nominalMpa, int cementKg, int sandKg,
                  int gravelKg, int waterLitres) {
        this.europeanClass = europeanClass;
        this.nominalMpa = nominalMpa;
        this.cementKg = cementKg;
        this.sandKg = sandKg;
        this.gravelKg = gravelKg;
        this.waterLitres = waterLitres;
    }

    public String europeanClass() { return europeanClass; }
    public int nominalMpa() { return nominalMpa; }
    public int cementKg() { return cementKg; }
    public int sandKg() { return sandKg; }
    public int gravelKg() { return gravelKg; }
    public int waterLitres() { return waterLitres; }
    public double nominalWaterCementRatio() {
        return (double) waterLitres / cementKg;
    }

    public static ConcreteGrade parse(String input) {
        if (input == null) throw new IllegalArgumentException("Missing grade");
        String normalized = input.trim().toUpperCase(Locale.ROOT);
        for (ConcreteGrade grade : values()) {
            if (grade.name().equals(normalized)
                    || grade.europeanClass.toUpperCase(Locale.ROOT).equals(normalized)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("Unsupported concrete grade: " + input);
    }
}
