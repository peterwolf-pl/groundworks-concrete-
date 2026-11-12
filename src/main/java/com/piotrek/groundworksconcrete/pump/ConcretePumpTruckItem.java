package com.piotrek.groundworksconcrete.pump;

import com.piotrek.groundworksconcrete.vehicle.ConcreteTruckRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Item for placing a mobile boom pump on a clear, loaded ground surface. */
public final class ConcretePumpTruckItem extends Item {
    public ConcretePumpTruckItem(Properties properties){super(properties);}

    @Override
    public InteractionResult useOn(UseOnContext ctx){
        Level level=ctx.getLevel();
        if(level.isClientSide())return InteractionResult.SUCCESS;
        BlockPos p=ctx.getClickedPos().relative(ctx.getClickedFace());
        if(!level.hasChunkAt(p))return InteractionResult.FAIL;
        Vec3 at=Vec3.atBottomCenterOf(p);
        if(!level.noCollision(ConcreteTruckRegistry.PUMP_TRUCK.getDimensions()
                .makeBoundingBox(at)))return InteractionResult.FAIL;
        ConcretePumpTruckEntity pump=ConcreteTruckRegistry.PUMP_TRUCK.create(
                (ServerLevel)level,EntitySpawnReason.SPAWN_ITEM_USE);
        if(pump==null)return InteractionResult.FAIL;
        float yaw=ctx.getPlayer()==null?0F:ctx.getPlayer().getYRot();
        pump.snapTo(at.x,at.y,at.z,yaw,0F);
        pump.setYBodyRot(yaw);
        pump.setYHeadRot(yaw);
        if(!level.addFreshEntity(pump))return InteractionResult.FAIL;
        Player player=ctx.getPlayer();
        if(player!=null&&!player.getAbilities().instabuild)ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
