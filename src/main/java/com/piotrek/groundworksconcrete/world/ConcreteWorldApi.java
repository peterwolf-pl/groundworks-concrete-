package com.piotrek.groundworksconcrete.world;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Future stationary mixer / mixer-truck / boom-pump integration API.
 * No inventory is modified here: caller must deduct only accepted units.
 */
public final class ConcreteWorldApi {
    private ConcreteWorldApi() {}

    public static ConcreteFlowWorld.PourResult pour(ServerLevel level, BlockPos at,
                                                    ConcreteBatch incoming) {
        return ConcreteFlowWorld.get(level).pour(level, at, incoming);
    }

    /** Groundworks convention: one full cubic block equals exactly 512 units. */
    public static int unitsPerBlock() {
        return ConcreteBatch.UNITS_PER_CUBIC_METRE;
    }
}
