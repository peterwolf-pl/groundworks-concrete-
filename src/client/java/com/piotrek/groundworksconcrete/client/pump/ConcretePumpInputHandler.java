package com.piotrek.groundworksconcrete.client.pump;

import com.piotrek.groundworksconcrete.network.ConcretePumpInputPayload;
import com.piotrek.groundworksconcrete.pump.ConcretePumpTruckEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/** Packets contain operator intent only. The server validates each action. */
public final class ConcretePumpInputHandler {
    private static float prevThrottle,prevSteer,prevSlew,prevJoint;
    private static int selectedJoint,keepalive;
    private ConcretePumpInputHandler(){}

    public static void tick(Minecraft client){
        if(client.player==null
                ||!(client.player.getVehicle() instanceof ConcretePumpTruckEntity)){
            prevThrottle=prevSteer=prevSlew=prevJoint=0;
            selectedJoint=0;keepalive=0;
            return;
        }
        float throttle=(client.options.keyUp.isDown()?1F:0F)
                -(client.options.keyDown.isDown()?1F:0F);
        float steer=(client.options.keyRight.isDown()?1F:0F)
                -(client.options.keyLeft.isDown()?1F:0F);
        float slew=(ConcretePumpKeys.SLEW_RIGHT.isDown()?1F:0F)
                -(ConcretePumpKeys.SLEW_LEFT.isDown()?1F:0F);
        float joint=(ConcretePumpKeys.JOINT_UP.isDown()?1F:0F)
                -(ConcretePumpKeys.JOINT_DOWN.isDown()?1F:0F);

        boolean changedJoint=false;
        while(ConcretePumpKeys.SELECT_NEXT.consumeClick()){
            selectedJoint=(selectedJoint+1)%5;
            changedJoint=true;
        }
        boolean legs=ConcretePumpKeys.LEGS.consumeClick();
        boolean stow=ConcretePumpKeys.STOW.consumeClick();
        boolean lights=ConcretePumpKeys.LIGHTS.consumeClick();
        boolean horn=ConcretePumpKeys.HORN.consumeClick();
        boolean axesChanged=prevThrottle!=throttle||prevSteer!=steer
                ||prevSlew!=slew||prevJoint!=joint;
        if(axesChanged||changedJoint||legs||stow||lights||horn||--keepalive<=0){
            ClientPlayNetworking.send(new ConcretePumpInputPayload(
                    throttle,steer,slew,joint,selectedJoint,legs,lights,horn,stow));
            keepalive=3;
            prevThrottle=throttle;prevSteer=steer;
            prevSlew=slew;prevJoint=joint;
        }
    }
}
