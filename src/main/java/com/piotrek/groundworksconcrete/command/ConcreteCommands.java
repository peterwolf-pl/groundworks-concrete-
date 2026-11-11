package com.piotrek.groundworksconcrete.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.world.ConcreteCuring;
import com.piotrek.groundworksconcrete.world.ConcreteFlowCell;
import com.piotrek.groundworksconcrete.world.ConcreteFlowWorld;
import com.piotrek.groundworksconcrete.world.ConcreteWorldApi;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;

/** Stage 2: bounded admin pouring, inspection, and Stage 1 recipe previews. */
public final class ConcreteCommands {
    private ConcreteCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        var recipes = Commands.literal("recipe");
        var previews = Commands.literal("preview");
        var pours = Commands.literal("pour");
        for (ConcreteGrade grade : ConcreteGrade.values()) {
            String label = grade.name().toLowerCase(Locale.ROOT);
            recipes.then(Commands.literal(label).executes(ctx -> showRecipe(ctx, grade)));
            previews.then(Commands.literal(label)
                    .then(Commands.argument("units", IntegerArgumentType.integer(1, 512_000))
                            .executes(ctx -> preview(ctx, grade,
                                    IntegerArgumentType.getInteger(ctx, "units")))));
            pours.then(Commands.literal(label)
                    .then(Commands.argument("units", IntegerArgumentType.integer(1, 32768))
                            .executes(ctx -> pour(ctx, grade,
                                    IntegerArgumentType.getInteger(ctx, "units"), getTarget(ctx)))
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                    .executes(ctx -> pour(ctx, grade,
                                            IntegerArgumentType.getInteger(ctx, "units"),
                                            BlockPosArgument.getLoadedBlockPos(ctx, "pos"))))));
        }

        dispatcher.register(Commands.literal("groundworks")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("concrete")
                        .then(recipes)
                        .then(previews)
                        .then(pours)
                        .then(Commands.literal("inspect")
                                .executes(ctx -> inspect(ctx, getTarget(ctx)))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> inspect(ctx,
                                                BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                        .then(Commands.literal("stats")
                                .executes(ctx -> stats(ctx)))));
    }

    private static BlockPos getTarget(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getEntity() instanceof Player player) {
            HitResult hit = player.pick(6.0D, 0.0F, false);
            if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos target = blockHit.getBlockPos();
                // Inspect targeted concrete; pour into the air above targeted formwork.
                return target.above();
            }
        }
        return BlockPos.containing(ctx.getSource().getPosition());
    }

    private static int pour(CommandContext<CommandSourceStack> ctx, ConcreteGrade grade,
                            int units, BlockPos at) {
        ConcreteBatch batch = ConcreteBatch.fresh(grade, units, 1.0);
        ConcreteFlowWorld.PourResult result = ConcreteWorldApi.pour(ctx.getSource().getLevel(),
                at, batch);
        if (result.acceptedUnits() == 0) {
            ctx.getSource().sendFailure(Component.literal("Pour blocked at " + at.toShortString()));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Poured " + result.acceptedUnits() + " units of " + grade.name()
                        + " at " + at.toShortString() + " (rejected "
                        + result.rejectedUnits() + ")"), true);
        return result.acceptedUnits();
    }

    private static int inspect(CommandContext<CommandSourceStack> ctx, BlockPos at) {
        ConcreteFlowWorld storage = ConcreteFlowWorld.get(ctx.getSource().getLevel());
        ConcreteFlowCell cell = storage.getCell(at);
        // The default target is above the looked-at block. If absent,
        // also inspect the block directly below for convenience.
        if (cell == null) {
            at = at.below();
            cell = storage.getCell(at);
        }
        if (cell == null) {
            ctx.getSource().sendFailure(Component.literal("No fresh concrete at selected position"));
            return 0;
        }
        long age = ConcreteCuring.elapsed(ctx.getSource().getLevel().getGameTime(), cell.pouredAt());
        ConcreteFlowCell selected = cell;
        BlockPos pos = at;
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "Concrete %s at %s: %d/512 units, age %d/24000 ticks, W/C %.3f, quality %.2f",
                selected.batch().grade().name(), pos.toShortString(),
                selected.units(), age, selected.batch().ingredients().waterCementRatio(),
                selected.batch().recipeQuality())), false);
        return 1;
    }

    private static int stats(CommandContext<CommandSourceStack> ctx) {
        ConcreteFlowWorld storage = ConcreteFlowWorld.get(ctx.getSource().getLevel());
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Wet concrete: " + storage.activeCellCount()
                        + " cells, " + storage.activeUnits() + " volume units"), false);
        return 1;
    }

    private static int showRecipe(CommandContext<CommandSourceStack> ctx, ConcreteGrade grade) {
        ctx.getSource().sendSuccess(() -> Component.literal(
                grade.name() + " (" + grade.europeanClass() + ") per 1 m3: cement "
                        + grade.cementKg() + " kg, sand " + grade.sandKg()
                        + " kg, gravel " + grade.gravelKg() + " kg, water "
                        + grade.waterLitres() + " l. W/C = "
                        + String.format(Locale.ROOT, "%.3f",
                        grade.nominalWaterCementRatio())), false);
        return 1;
    }

    private static int preview(CommandContext<CommandSourceStack> ctx, ConcreteGrade grade, int units) {
        ConcreteBatch batch = ConcreteBatch.fresh(grade, units, 1);
        var a = batch.ingredients();
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "%s: %d units (%.3f m3), cement %d g, sand %d g, gravel %d g, water %d ml",
                grade.name(), batch.volumeUnits(), batch.volumeCubicMetres(),
                a.cementGrams(), a.sandGrams(), a.gravelGrams(), a.waterMillilitres())), false);
        return 1;
    }
}
