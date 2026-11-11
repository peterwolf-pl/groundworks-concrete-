package com.piotrek.groundworksconcrete.world;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;

/**
 * Pure deterministic gravity-first fluid simulation. Deliberately NOT a vanilla
 * lava source: every step moves exact 1/512 m³ volume and never creates material.
 */
public final class ConcreteFlowPhysics {
    public static final int CELL_CAPACITY = ConcreteBatch.UNITS_PER_CUBIC_METRE;
    public static final int HORIZONTAL_HEAD_UNITS = 64;
    public static final int MAX_GRAVITY_TRANSFER = 128;
    public static final int MAX_HORIZONTAL_TRANSFER = 48;

    public record Position(int x, int y, int z) {
        public Position offset(int dx, int dy, int dz) {
            return new Position(x + dx, y + dy, z + dz);
        }
    }

    /** World adapter may refuse destinations on chunk boundaries or inside solid formwork. */
    public interface Grid {
        ConcreteFlowCell get(Position pos);
        boolean canAccept(Position pos);
        void put(Position pos, ConcreteFlowCell cell);
    }

    private ConcreteFlowPhysics() {}

    /** Process one cell at the caller's bounded tick interval. */
    public static boolean step(Grid grid, Position origin, long now) {
        ConcreteFlowCell source = grid.get(origin);
        if (source == null
                || !ConcreteCuring.mayFlow(ConcreteCuring.elapsed(now, source.pouredAt()))) {
            return false;
        }

        // Prefer down, then equalization with lateral cells. Never move upward.
        if (transfer(grid, origin, origin.offset(0, -1, 0), MAX_GRAVITY_TRANSFER)) {
            return true;
        }

        boolean changed = false;
        for (int[] d : new int[][] {{1, 0, 0}, {0, 0, 1}, {-1, 0, 0}, {0, 0, -1}}) {
            source = grid.get(origin);
            if (source == null) break;
            Position neighbor = origin.offset(d[0], 0, d[2]);
            ConcreteFlowCell target = grid.get(neighbor);
            int existing = target == null ? 0 : target.units();
            int difference = source.units() - existing - HORIZONTAL_HEAD_UNITS;
            if (difference < 2) continue;

            int take = Math.min(MAX_HORIZONTAL_TRANSFER, difference / 2);
            if (transfer(grid, origin, neighbor, take)) changed = true;
        }
        return changed;
    }

    private static boolean transfer(Grid grid, Position from, Position to, int max) {
        if (max <= 0 || !grid.canAccept(to)) return false;
        ConcreteFlowCell source = grid.get(from);
        if (source == null) return false;
        ConcreteFlowCell target = grid.get(to);
        if (target != null && target.batch().grade() != source.batch().grade()) return false;

        int free = CELL_CAPACITY - (target == null ? 0 : target.units());
        int toMove = Math.min(max, Math.min(free, source.units()));
        if (toMove <= 0) return false;

        ConcreteFlowCell.Split split = source.split(toMove);
        ConcreteFlowCell destination = target == null ? split.moved() : target.combine(split.moved());
        grid.put(to, destination);
        grid.put(from, split.remaining());
        return true;
    }
}
