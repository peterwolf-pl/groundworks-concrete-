package com.piotrek.groundworksconcrete.world;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ConcreteFlowPhysicsTest {
    private final class BoxWorld implements ConcreteFlowPhysics.Grid {
        final Map<ConcreteFlowPhysics.Position, ConcreteFlowCell> cells = new HashMap<>();
        final Set<ConcreteFlowPhysics.Position> blocked = new java.util.HashSet<>();
        @Override public ConcreteFlowCell get(ConcreteFlowPhysics.Position pos) {
            return cells.get(pos);
        }
        @Override public boolean canAccept(ConcreteFlowPhysics.Position pos) {
            return pos.y() >= 0 && !blocked.contains(pos);
        }
        @Override public void put(ConcreteFlowPhysics.Position pos, ConcreteFlowCell cell) {
            if (cell == null) cells.remove(pos);
            else cells.put(pos, cell);
        }
        int total() {
            return cells.values().stream().mapToInt(ConcreteFlowCell::units).sum();
        }
        long[] ingredients() {
            long[] x = new long[4];
            cells.values().forEach(c -> {
                var a = c.batch().ingredients();
                x[0] += a.cementGrams(); x[1] += a.sandGrams();
                x[2] += a.gravelGrams(); x[3] += a.waterMillilitres();
            });
            return x;
        }
    }

    private static final ConcreteFlowPhysics.Position START =
            new ConcreteFlowPhysics.Position(0, 1, 0);

    private BoxWorld initial() {
        BoxWorld grid = new BoxWorld();
        grid.put(START, new ConcreteFlowCell(
                ConcreteBatch.fresh(ConcreteGrade.B20, 512, 1), 100L));
        return grid;
    }

    @Test void downwardFlowPreservesEntireBatch() {
        BoxWorld world = initial();
        long[] initial = world.ingredients();
        assertTrue(ConcreteFlowPhysics.step(world, START, 100L));
        assertEquals(512, world.total());
        assertArrayEquals(initial, world.ingredients());
        assertEquals(128, world.get(new ConcreteFlowPhysics.Position(0, 0, 0)).units());
    }

    @Test void formworkWallsContainFlowWithoutLeaksOrInfiniteSources() {
        BoxWorld grid = initial();
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++)
                if (Math.abs(x) >= 2 || Math.abs(z) >= 2)
                    grid.blocked.add(new ConcreteFlowPhysics.Position(x, 0, z));
        long[] before = grid.ingredients();
        for (int i = 0; i < 350; i++) {
            var positions = java.util.List.copyOf(grid.cells.keySet());
            for (var pos : positions) ConcreteFlowPhysics.step(grid, pos, 100 + 8L * i);
        }
        assertEquals(512, grid.total());
        assertArrayEquals(before, grid.ingredients());
        assertTrue(grid.cells.keySet().stream().noneMatch(
                p -> p.y() == 0 && (Math.abs(p.x()) >= 2 || Math.abs(p.z()) >= 2)));
    }

    @Test void oldConcreteDoesNotFlow() {
        BoxWorld world = initial();
        assertFalse(ConcreteFlowPhysics.step(world, START, 20_100L));
        assertEquals(512, world.total());
    }

    @Test void differentGradesDoNotMix() {
        BoxWorld world = initial();
        var below = new ConcreteFlowPhysics.Position(0, 0, 0);
        world.put(below, new ConcreteFlowCell(
                ConcreteBatch.fresh(ConcreteGrade.B30, 100, 1), 100));
        ConcreteFlowPhysics.step(world, START, 100);
        assertEquals(100, world.get(below).units());
        assertEquals(612, world.total());
    }

    @Test void ageAndCureBoundaries() {
        assertEquals(0, ConcreteCuring.tone(7_999));
        assertEquals(1, ConcreteCuring.tone(8_000));
        assertEquals(2, ConcreteCuring.tone(16_000));
        assertTrue(ConcreteCuring.mayFlow(19_999));
        assertFalse(ConcreteCuring.mayFlow(20_000));
        assertFalse(ConcreteCuring.isHardened(23_999));
        assertTrue(ConcreteCuring.isHardened(24_000));
        assertEquals(8_000, ConcreteCuring.nextBoundary(0, 7000));
        assertEquals(24_000, ConcreteCuring.nextBoundary(0, 22_000));
    }
}
