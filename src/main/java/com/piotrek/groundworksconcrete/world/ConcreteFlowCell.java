package com.piotrek.groundworksconcrete.world;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;

import java.util.Objects;

/** One dimension-local concrete cell, occupying at most 512 volumetric units. */
public record ConcreteFlowCell(ConcreteBatch batch, long pouredAt) {
    public ConcreteFlowCell {
        Objects.requireNonNull(batch, "batch");
        if (batch.phase() != ConcreteBatch.Phase.FRESH) {
            throw new IllegalArgumentException("Only fresh concrete can flow");
        }
        if (batch.volumeUnits() > ConcreteBatch.UNITS_PER_CUBIC_METRE) {
            throw new IllegalArgumentException("Cell capacity exceeds one block");
        }
    }

    public int units() { return batch.volumeUnits(); }

    public ConcreteFlowCell combine(ConcreteFlowCell other) {
        return new ConcreteFlowCell(batch.combine(other.batch), Math.min(pouredAt, other.pouredAt));
    }

    public Split split(int requested) {
        ConcreteBatch.Split split = batch.split(requested);
        return new Split(new ConcreteFlowCell(split.taken(), pouredAt),
                split.remaining() == null ? null : new ConcreteFlowCell(split.remaining(), pouredAt));
    }

    public record Split(ConcreteFlowCell moved, ConcreteFlowCell remaining) { }
}
