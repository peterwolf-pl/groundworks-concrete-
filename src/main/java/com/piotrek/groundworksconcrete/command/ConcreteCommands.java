package com.piotrek.groundworksconcrete.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Stage 1 read-only recipe/batch previews. Vehicle filling comes in a later stage.
 * Registers a child of the existing Groundworks root command.
 */
public final class ConcreteCommands {
    private ConcreteCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        var recipes = Commands.literal("recipe");
        var previews = Commands.literal("preview");
        for (ConcreteGrade grade : ConcreteGrade.values()) {
            recipes.then(Commands.literal(grade.name().toLowerCase())
                    .executes(ctx -> showRecipe(ctx, grade)));
            previews.then(Commands.literal(grade.name().toLowerCase())
                    .then(Commands.argument("units", IntegerArgumentType.integer(1, 512_000))
                            .executes(ctx -> preview(ctx, grade,
                                    IntegerArgumentType.getInteger(ctx, "units")))));
        }
        dispatcher.register(Commands.literal("groundworks")
                .then(Commands.literal("concrete")
                        .then(recipes)
                        .then(previews)));
    }

    private static int showRecipe(CommandContext<CommandSourceStack> ctx, ConcreteGrade grade) {
        ctx.getSource().sendSuccess(() -> Component.literal(
                grade.name() + " (" + grade.europeanClass() + ") per 1 m3: cement "
                        + grade.cementKg() + " kg, sand " + grade.sandKg()
                        + " kg, gravel " + grade.gravelKg() + " kg, water "
                        + grade.waterLitres() + " l. W/C = "
                        + String.format(java.util.Locale.ROOT, "%.3f",
                        grade.nominalWaterCementRatio())), false);
        return 1;
    }

    private static int preview(CommandContext<CommandSourceStack> ctx, ConcreteGrade grade, int units) {
        ConcreteBatch batch = ConcreteBatch.fresh(grade, units, 1);
        var a = batch.ingredients();
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(java.util.Locale.ROOT,
                "%s: %d units (%.3f m3), cement %d g, sand %d g, gravel %d g, water %d ml",
                grade.name(), batch.volumeUnits(), batch.volumeCubicMetres(),
                a.cementGrams(), a.sandGrams(), a.gravelGrams(), a.waterMillilitres())), false);
        return 1;
    }
}
