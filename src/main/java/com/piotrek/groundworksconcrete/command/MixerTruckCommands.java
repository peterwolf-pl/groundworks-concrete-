package com.piotrek.groundworksconcrete.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.vehicle.MixerTruckEntity;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/** Operator-only tools for Stage 4 functional tests. Not normal survival loading. */
public final class MixerTruckCommands {
    private static final double SEARCH_RADIUS=18D;
    private MixerTruckCommands(){}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registry,Commands.CommandSelection selection){
        var fill=Commands.literal("fill");
        for(ConcreteGrade grade:ConcreteGrade.values()) {
            fill.then(Commands.literal(grade.name().toLowerCase(Locale.ROOT))
                    .then(Commands.argument("units",IntegerArgumentType.integer(1,
                            MixerTruckEntity.CAPACITY_UNITS))
                            .executes(ctx->fill(ctx,grade,
                                    IntegerArgumentType.getInteger(ctx,"units")))));
        }
        dispatcher.register(Commands.literal("groundworks")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("concrete")
                        .then(Commands.literal("truck")
                                .then(fill)
                                .then(Commands.literal("inspect")
                                        .executes(MixerTruckCommands::inspect))
                                .then(Commands.literal("rpm")
                                        .then(Commands.argument("rpm",
                                                FloatArgumentType.floatArg(0F,12F))
                                                .executes(MixerTruckCommands::rpm))))));
    }

    @Nullable
    private static MixerTruckEntity nearest(CommandSourceStack source){
        Vec3 p=source.getPosition();
        AABB area=new AABB(p.x-SEARCH_RADIUS,p.y-SEARCH_RADIUS,p.z-SEARCH_RADIUS,
                p.x+SEARCH_RADIUS,p.y+SEARCH_RADIUS,p.z+SEARCH_RADIUS);
        return source.getLevel().getEntitiesOfClass(MixerTruckEntity.class,area,
                    truck->!truck.isRemoved())
                .stream().min(Comparator.comparingDouble(t->t.distanceToSqr(p))).orElse(null);
    }

    private static int fill(CommandContext<CommandSourceStack> ctx,ConcreteGrade grade,int units){
        MixerTruckEntity truck=nearest(ctx.getSource());
        if(truck==null){
            ctx.getSource().sendFailure(Component.literal("No concrete mixer truck within 18 blocks"));
            return 0;
        }
        int accepted=truck.receiveFresh(ConcreteBatch.fresh(grade,units,1D));
        if(accepted<=0){
            ctx.getSource().sendFailure(Component.literal(
                    "Truck full or carrying a different concrete grade"));
            return 0;
        }
        ctx.getSource().sendSuccess(()->Component.literal(
                "Loaded "+accepted+" units of "+grade.name()+" into concrete mixer truck "
                        +truck.getUUID()+"; "+truck.storedUnits()+"/"+MixerTruckEntity.CAPACITY_UNITS),
                true);
        return accepted;
    }

    private static int inspect(CommandContext<CommandSourceStack> ctx){
        MixerTruckEntity truck=nearest(ctx.getSource());
        if(truck==null){
            ctx.getSource().sendFailure(Component.literal("No mixer truck nearby"));
            return 0;
        }
        String message=String.format(Locale.ROOT,
                "Mixer truck %s: %d/%d units (%.2f m3), drum %.1f RPM %s, headlights %s",
                truck.displayedGrade(),truck.storedUnits(),MixerTruckEntity.CAPACITY_UNITS,
                truck.storedUnits()/512D,truck.getDrumRpm(),
                truck.drumSpinning()?"mixing":"off",truck.lightsOn()?"on":"off");
        ctx.getSource().sendSuccess(()->Component.literal(message),false);
        return 1;
    }

    private static int rpm(CommandContext<CommandSourceStack> ctx){
        MixerTruckEntity truck=nearest(ctx.getSource());
        if(truck==null){
            ctx.getSource().sendFailure(Component.literal("No mixer truck nearby"));
            return 0;
        }
        float rpm=FloatArgumentType.getFloat(ctx,"rpm");
        truck.setDrumRpm(rpm);
        ctx.getSource().sendSuccess(()->Component.literal("Drum speed set to "+rpm+" RPM"),true);
        return 1;
    }
}
