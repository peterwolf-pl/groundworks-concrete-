package com.piotrek.groundworksconcrete.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.pump.ConcretePumpTruckEntity;
import com.piotrek.groundworksconcrete.pump.PumpBoomRig;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Locale;

/**
 * Operator commands for direct functional testing of Stage 6 boom hydraulics.
 * Stage 7 adds a paired wireless control and real concrete pumping through hose.
 */
public final class ConcretePumpCommands {
    private static final double RADIUS=32.0D;
    private ConcretePumpCommands(){}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext access,Commands.CommandSelection selection){
        var fills=Commands.literal("fill");
        for(ConcreteGrade grade:ConcreteGrade.values()){
            fills.then(Commands.literal(grade.name().toLowerCase(Locale.ROOT))
                    .then(Commands.argument("units",IntegerArgumentType.integer(1,
                                    ConcretePumpTruckEntity.HOPPER_CAPACITY))
                            .executes(ctx->fill(ctx,grade,IntegerArgumentType.getInteger(ctx,"units")))));
        }
        dispatcher.register(Commands.literal("groundworks")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("concrete")
                        .then(Commands.literal("pump")
                                .then(Commands.literal("inspect")
                                        .executes(ConcretePumpCommands::inspect))
                                .then(Commands.literal("deploy")
                                        .executes(ctx->legs(ctx,true)))
                                .then(Commands.literal("retract")
                                        .executes(ctx->legs(ctx,false)))
                                .then(Commands.literal("stow")
                                        .executes(ConcretePumpCommands::stow))
                                .then(Commands.literal("start")
                                        .executes(ctx->power(ctx,true)))
                                .then(Commands.literal("stop")
                                        .executes(ctx->power(ctx,false)))
                                .then(Commands.literal("estop")
                                        .executes(ConcretePumpCommands::estop))
                                .then(Commands.literal("reset")
                                        .executes(ConcretePumpCommands::reset))
                                .then(Commands.literal("unpair")
                                        .executes(ConcretePumpCommands::unpair))
                                .then(Commands.literal("rate")
                                        .then(Commands.argument("units",IntegerArgumentType.integer(1,32))
                                                .executes(ConcretePumpCommands::rate)))
                                .then(Commands.literal("target")
                                        .then(Commands.argument("pos",BlockPosArgument.blockPos())
                                                .executes(ConcretePumpCommands::target)))
                                .then(Commands.literal("slew")
                                        .then(Commands.argument("degrees",DoubleArgumentType.doubleArg(
                                                -PumpBoomRig.MAX_SLEW,PumpBoomRig.MAX_SLEW))
                                                .executes(ctx->slew(ctx,
                                                        DoubleArgumentType.getDouble(ctx,"degrees")))))
                                .then(Commands.literal("joint")
                                        .then(Commands.argument("index",IntegerArgumentType.integer(0,4))
                                                .then(Commands.argument("degrees",
                                                        DoubleArgumentType.doubleArg(-175,175))
                                                        .executes(ConcretePumpCommands::joint))))
                                .then(fills))));
    }

    @Nullable
    private static ConcretePumpTruckEntity nearest(CommandSourceStack source){
        Vec3 at=source.getPosition();
        AABB bounds=new AABB(at.x-RADIUS,at.y-RADIUS,at.z-RADIUS,
                at.x+RADIUS,at.y+RADIUS,at.z+RADIUS);
        return source.getLevel().getEntitiesOfClass(
                ConcretePumpTruckEntity.class,bounds,p->!p.isRemoved())
                .stream().min(Comparator.comparingDouble(t->t.distanceToSqr(at))).orElse(null);
    }

    @Nullable
    private static ConcretePumpTruckEntity require(CommandSourceStack source){
        ConcretePumpTruckEntity pump=nearest(source);
        if(pump==null)source.sendFailure(Component.literal(
                "No concrete pump truck within 32 blocks"));
        return pump;
    }

    private static int legs(CommandContext<CommandSourceStack> ctx,boolean deploy){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        if(!pump.requestOutriggers(deploy)){
            ctx.getSource().sendFailure(Component.literal(
                    "Cannot change stabilizers: stop driving or stow boom first"));
            return 0;
        }
        ctx.getSource().sendSuccess(()->Component.literal(
                "Stabilizers "+(deploy?"deploying":"retracting")),true);
        return 1;
    }

    private static int joint(CommandContext<CommandSourceStack> ctx){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        int id=IntegerArgumentType.getInteger(ctx,"index");
        double angle=DoubleArgumentType.getDouble(ctx,"degrees");
        if(!pump.requestJoint(id,angle)){
            ctx.getSource().sendFailure(Component.literal(
                    "Cannot actuate boom: all four outriggers must be locked and supported"));
            return 0;
        }
        ctx.getSource().sendSuccess(()->Component.literal(
                "Requested boom joint "+(id+1)+" = "+angle+" degrees"),true);
        return 1;
    }

    private static int slew(CommandContext<CommandSourceStack> ctx,double angle){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        if(!pump.requestSlew(angle)){
            ctx.getSource().sendFailure(Component.literal("Support all four legs first"));
            return 0;
        }
        ctx.getSource().sendSuccess(()->Component.literal(
                "Requested hydraulic turntable slew = "+angle+" degrees"),true);
        return 1;
    }

    private static int stow(CommandContext<CommandSourceStack> ctx){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        pump.stowBoom();
        ctx.getSource().sendSuccess(()->Component.literal(
                "Boom folding targets set. Retract stabilizers only after fully stowed."),true);
        return 1;
    }

    private static int power(CommandContext<CommandSourceStack> ctx,boolean enabled){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        if(!pump.requestPumping(enabled)){
            ctx.getSource().sendFailure(Component.literal(
                    "Cannot start pumping: verify legs, non-stowed boom, hopper and E-STOP"));
            return 0;
        }
        ctx.getSource().sendSuccess(()->Component.literal(
                enabled?"Pump running":"Pump stopped"),true);
        return 1;
    }

    private static int estop(CommandContext<CommandSourceStack> ctx){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        pump.emergencyStop();
        ctx.getSource().sendSuccess(()->Component.literal("EMERGENCY STOP activated"),true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        if(!pump.resetEmergencyStop()){
            ctx.getSource().sendFailure(Component.literal(
                    "Cannot reset emergency stop until all outriggers are supported"));
            return 0;
        }
        ctx.getSource().sendSuccess(()->Component.literal("E-STOP released, pump remains off"),true);
        return 1;
    }

    private static int unpair(CommandContext<CommandSourceStack> ctx){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        pump.clearRemotePairing();
        ctx.getSource().sendSuccess(()->Component.literal("Pump remote pairing cleared"),true);
        return 1;
    }

    private static int rate(CommandContext<CommandSourceStack> ctx){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        int value=IntegerArgumentType.getInteger(ctx,"units");
        pump.setPumpRate(value);
        ctx.getSource().sendSuccess(()->Component.literal(
                "Pump flow rate "+value+" units per tick"),true);
        return 1;
    }

    private static int target(CommandContext<CommandSourceStack> ctx){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        BlockPos pos=BlockPosArgument.getLoadedBlockPos(ctx,"pos");
        if(!pump.requestAim(new PumpBoomRig.Point(pos.getX()+0.5,
                pos.getY()+0.2,pos.getZ()+0.5))){
            ctx.getSource().sendFailure(Component.literal(
                    "Hose target unreachable or outriggers not locked"));
            return 0;
        }
        ctx.getSource().sendSuccess(()->Component.literal(
                "Boom IK target selected, actuators moving to "+pos.toShortString()),true);
        return 1;
    }

    private static int fill(CommandContext<CommandSourceStack> ctx,ConcreteGrade grade,int units){
        ConcretePumpTruckEntity pump=require(ctx.getSource());
        if(pump==null)return 0;
        int moved=pump.receiveConcrete(ConcreteBatch.fresh(grade,units,1D));
        if(moved<=0){
            ctx.getSource().sendFailure(Component.literal(
                    "Pump hopper full or contains incompatible grade"));
            return 0;
        }
        ctx.getSource().sendSuccess(()->Component.literal(
                "Pump hopper +"+moved+" units "+grade.name()
                        +" ("+pump.hopperUnits()+"/"+pump.hopperCapacity()+")"),true);
        return moved;
    }

    private static int inspect(CommandContext<CommandSourceStack> ctx){
        ConcretePumpTruckEntity p=require(ctx.getSource());
        if(p==null)return 0;
        var tip=p.boomPose().tip();
        String msg=String.format(Locale.ROOT,
                "Pump: legs %s, support %s, boom %s, slew %.1f, tip %.2f %.2f %.2f, hopper %s %d/%d, pumping %s, rate %d, E-STOP %s",
                p.outriggersRequested()?"DEPLOYED/DEPLOYING":"RETRACTED",
                p.isRigSupported()?"LOCKED":"NOT LOCKED",
                p.boomStowed()?"STOWED":"ACTIVE",
                p.slewAngle(),tip.x(),tip.y(),tip.z(),
                p.hopperGrade(),p.hopperUnits(),p.hopperCapacity(),
                p.pumpEnabled()?"ON":"OFF",p.pumpRate(),
                p.emergencyStopped()?"LATCHED":"CLEAR");
        ctx.getSource().sendSuccess(()->Component.literal(msg),false);
        return 1;
    }
}
