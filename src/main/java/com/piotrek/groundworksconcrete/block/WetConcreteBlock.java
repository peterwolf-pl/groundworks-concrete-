package com.piotrek.groundworksconcrete.block;

import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Wet concrete: no lava interactions, burning, ignition or collision. */
public final class WetConcreteBlock extends ConcreteLayerBlock {
    public static final IntegerProperty TONE = IntegerProperty.create("tone", 0, 2);

    public WetConcreteBlock(ConcreteGrade grade, Properties properties) {
        super(grade, properties);
        registerDefaultState(defaultBlockState().setValue(TONE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TONE);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return Shapes.empty();
    }
}
