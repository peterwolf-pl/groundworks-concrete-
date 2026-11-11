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

/** Partial-height concrete block. Exact volume is stored server-side, not in LEVEL. */
public abstract class ConcreteLayerBlock extends Block {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 8);
    private final ConcreteGrade grade;

    protected ConcreteLayerBlock(ConcreteGrade grade, Properties properties) {
        super(properties);
        this.grade = grade;
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 8));
    }

    public ConcreteGrade grade() { return grade; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos,
                                  CollisionContext context) {
        return Shapes.box(0.0D, 0.0D, 0.0D, 1.0D,
                state.getValue(LEVEL) / 8.0D, 1.0D);
    }
}
