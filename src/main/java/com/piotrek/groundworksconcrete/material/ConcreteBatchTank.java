package com.piotrek.groundworksconcrete.material;

/**
 * Capacity-constrained container. Insert returns both accepted and rejected
 * quantities so callers can deduct only what actually entered the container.
 * Different grades and dry/fresh phases cannot be silently combined.
 */
public final class ConcreteBatchTank {
    private final int capacityUnits;
    private ConcreteBatch stored;

    public ConcreteBatchTank(int capacityUnits) {
        if (capacityUnits < 1) throw new IllegalArgumentException("Capacity must be positive");
        this.capacityUnits = capacityUnits;
    }

    public int capacityUnits() { return capacityUnits; }
    public int storedUnits() { return stored == null ? 0 : stored.volumeUnits(); }
    public ConcreteBatch snapshot() { return stored; }
    public int freeUnits() { return capacityUnits - storedUnits(); }

    public Transfer insert(ConcreteBatch offered) {
        if (offered == null) throw new IllegalArgumentException("Missing offered concrete");
        if (stored != null && (stored.grade() != offered.grade() || stored.phase() != offered.phase())) {
            return new Transfer(null, offered);
        }
        int acceptedUnits = Math.min(freeUnits(), offered.volumeUnits());
        if (acceptedUnits == 0) return new Transfer(null, offered);
        ConcreteBatch.Split split = offered.split(acceptedUnits);
        stored = stored == null ? split.taken() : stored.combine(split.taken());
        return new Transfer(split.taken(), split.remaining());
    }

    public ConcreteBatch drain(int requestedUnits) {
        if (requestedUnits < 1 || stored == null) return null;
        ConcreteBatch.Split split = stored.split(Math.min(requestedUnits, stored.volumeUnits()));
        stored = split.remaining();
        return split.taken();
    }

    public record Transfer(ConcreteBatch accepted, ConcreteBatch rejected) {
        public int acceptedUnits() { return accepted == null ? 0 : accepted.volumeUnits(); }
        public int rejectedUnits() { return rejected == null ? 0 : rejected.volumeUnits(); }
    }
}
