package com.piotrek.groundworksconcrete.client.input;

import com.piotrek.groundworksconcrete.network.MixerTruckInputPayload;
import com.piotrek.groundworksconcrete.vehicle.MixerTruckEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/** Sends steering and edge-triggered controls, never inventory writes. */
public final class MixerTruckInputHandler {
    private static float previousThrottle,previousSteer;
    private static int keepalive;
    private MixerTruckInputHandler(){}

    public static void tick(Minecraft client){
        if(client.player==null || !(client.player.getVehicle() instanceof MixerTruckEntity)){
            previousThrottle=previousSteer=0F;
            keepalive=0;
            return;
        }
        boolean forward=client.options.keyUp.isDown();
        boolean reverse=client.options.keyDown.isDown();
        boolean left=client.options.keyLeft.isDown();
        boolean right=client.options.keyRight.isDown();
        float throttle=(forward?1F:0F)-(reverse?1F:0F);
        float steer=(right?1F:0F)-(left?1F:0F);
        boolean drum=MixerTruckKeys.DRUM.consumeClick();
        boolean lights=MixerTruckKeys.LIGHTS.consumeClick();
        boolean horn=MixerTruckKeys.HORN.consumeClick();
        boolean changed=throttle!=previousThrottle || steer!=previousSteer;
        if(changed || drum || lights || horn || --keepalive<=0){
            ClientPlayNetworking.send(new MixerTruckInputPayload(
                    throttle,steer,drum,lights,horn));
            keepalive=3;
            previousThrottle=throttle;
            previousSteer=steer;
        }
    }
}
