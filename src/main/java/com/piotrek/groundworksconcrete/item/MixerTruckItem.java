package com.piotrek.groundworksconcrete.item;

import com.piotrek.groundworksconcrete.vehicle.ConcreteTruckRegistry;
import com.piotrek.groundworksconcrete.vehicle.MixerTruckEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Place a heavy truck only when the surrounding volume is clear. */
public final class MixerTruckItem extends Item {
    public MixerTruckItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        Vec3 at = new Vec3(target.getX()+0.5D,target.getY(),target.getZ()+0.5D);
        if (!level.noCollision(ConcreteTruckRegistry.MIXER_TRUCK.getDimensions()
                .makeBoundingBox(at))) return InteractionResult.FAIL;

        MixerTruckEntity truck = ConcreteTruckRegistry.MIXER_TRUCK.create(
                (ServerLevel) level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (truck == null) return InteractionResult.FAIL;
        float yaw = context.getPlayer() == null ? 0F : context.getPlayer().getYRot();
        truck.snapTo(at.x,at.y,at.z,yaw,0F);
        truck.setYHeadRot(yaw);
        truck.setYBodyRot(yaw);
        level.addFreshEntity(truck);
        Player player = context.getPlayer();
        ItemStack item = context.getItemInHand();
        if (player != null && !player.getAbilities().instabuild) item.shrink(1);
        return InteractionResult.CONSUME;
    }
}
