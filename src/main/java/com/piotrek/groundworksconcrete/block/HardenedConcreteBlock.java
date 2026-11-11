package com.piotrek.groundworksconcrete.block;

import com.piotrek.groundworksconcrete.material.ConcreteGrade;

/** Hardened concrete retains the fill level and has grade-dependent hardness. */
public final class HardenedConcreteBlock extends ConcreteLayerBlock {
    public HardenedConcreteBlock(ConcreteGrade grade, Properties properties) {
        super(grade, properties);
    }
}
