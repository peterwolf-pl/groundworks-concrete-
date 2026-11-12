package com.piotrek.groundworksconcrete.client.pump;

import com.piotrek.groundworksconcrete.item.ConcreteItems;
import com.piotrek.groundworksconcrete.network.PumpRemoteControlPayload;
import com.piotrek.groundworksconcrete.pump.ConcretePumpTruckEntity;
import com.piotrek.groundworksconcrete.pump.PumpRemoteSafety;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.util.Comparator;

/** Client-side remote target selection. Actual authorization is on the server. */
public final class PumpRemoteInputHandler {
    private static int selectedJoint,keepalive;
    private static float previousSlew,previousPitch;
    private PumpRemoteInputHandler(){}

    public static ConcretePumpTruckEntity nearbyPairedPump(Minecraft client){
        if(client.level==null||client.player==null)return null;
        String owner=client.player.getUUID().toString();
        double radius=PumpRemoteSafety.MAX_RANGE_BLOCKS;
        return client.level.getEntitiesOfClass(ConcretePumpTruckEntity.class,
                client.player.getBoundingBox().inflate(radius),
                p -> owner.equals(p.pairedOperatorId())
                        && client.player.distanceToSqr(p)<=radius*radius)
                .stream()
                .min(Comparator.comparingDouble(client.player::distanceToSqr))
                .orElse(null);
    }

    public static boolean isRemoteHeld(Minecraft client){
        return client.player!=null && (
                client.player.getMainHandItem().is(ConcreteItems.PUMP_REMOTE)
                || client.player.getOffhandItem().is(ConcreteItems.PUMP_REMOTE));
    }

    public static void tick(Minecraft client){
        if(!isRemoteHeld(client) || client.player.getVehicle()!=null){
            keepalive=0;previousSlew=0;previousPitch=0;selectedJoint=0;return;
        }
        ConcretePumpTruckEntity pump=nearbyPairedPump(client);
        if(pump==null){keepalive=0;return;}
        float slew=(ConcretePumpKeys.SLEW_RIGHT.isDown()?1F:0F)
                -(ConcretePumpKeys.SLEW_LEFT.isDown()?1F:0F);
        float pitch=(ConcretePumpKeys.JOINT_UP.isDown()?1F:0F)
                -(ConcretePumpKeys.JOINT_DOWN.isDown()?1F:0F);

        boolean jointChanged=false;
        while(ConcretePumpKeys.SELECT_NEXT.consumeClick()){
            selectedJoint=(selectedJoint+1)%5;jointChanged=true;
        }
        boolean pumping=PumpRemoteKeys.PUMP_TOGGLE.consumeClick();
        boolean stop=PumpRemoteKeys.EMERGENCY.consumeClick();
        boolean reset=PumpRemoteKeys.RESET.consumeClick();
        boolean aim=PumpRemoteKeys.AIM.consumeClick();
        int rateDelta=0;
        while(PumpRemoteKeys.RATE_UP.consumeClick())rateDelta++;
        while(PumpRemoteKeys.RATE_DOWN.consumeClick())rateDelta--;
        rateDelta=Math.clamp(rateDelta,-4,4);
        boolean stow=ConcretePumpKeys.STOW.consumeClick();
        boolean legs=ConcretePumpKeys.LEGS.consumeClick();

        boolean axesChanged=slew!=previousSlew||pitch!=previousPitch;
        if(axesChanged||jointChanged||pumping||stop||reset||aim||rateDelta!=0
                ||stow||legs||--keepalive<=0){
            ClientPlayNetworking.send(new PumpRemoteControlPayload(
                    pump.getId(),selectedJoint,slew,pitch,
                    pumping,rateDelta,stop,reset,stow,legs,aim));
            keepalive=3;previousSlew=slew;previousPitch=pitch;
        }
    }
}
