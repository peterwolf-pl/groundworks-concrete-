package com.piotrek.groundworksconcrete.client.input;

import com.piotrek.groundworksconcrete.network.MixerTruckInputPayload;
import com.piotrek.groundworksconcrete.vehicle.MixerTruckEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/**
 * Sends operator intent only. No inventory or direct world mutations from the client.
 * Edge-triggered drum, lights and direction changes plus held chute axes.
 */
public final class MixerTruckInputHandler {
    private static float lastThrottle,lastSteer,lastYaw,lastTilt;
    private static int keepalive;
    private MixerTruckInputHandler(){}

    public static void tick(Minecraft client){
        if(client.player==null || !(client.player.getVehicle() instanceof MixerTruckEntity)){
            lastThrottle=lastSteer=lastYaw=lastTilt=0F;
            keepalive=0;
            return;
        }

        float throttle=(client.options.keyUp.isDown()?1F:0F)
                -(client.options.keyDown.isDown()?1F:0F);
        float steer=(client.options.keyRight.isDown()?1F:0F)
                -(client.options.keyLeft.isDown()?1F:0F);
        float yaw=(MixerTruckKeys.CHUTE_RIGHT.isDown()?1F:0F)
                -(MixerTruckKeys.CHUTE_LEFT.isDown()?1F:0F);
        // Arrow Up raises the discharge tip (reduces downward tilt).
        float tilt=(MixerTruckKeys.CHUTE_DOWN.isDown()?1F:0F)
                -(MixerTruckKeys.CHUTE_UP.isDown()?1F:0F);

        boolean drum=MixerTruckKeys.DRUM.consumeClick();
        boolean reverse=MixerTruckKeys.REVERSE.consumeClick();
        boolean lights=MixerTruckKeys.LIGHTS.consumeClick();
        boolean horn=MixerTruckKeys.HORN.consumeClick();

        boolean changed=throttle!=lastThrottle || steer!=lastSteer
                || yaw!=lastYaw || tilt!=lastTilt;
        if(changed || drum || reverse || lights || horn || --keepalive<=0){
            ClientPlayNetworking.send(new MixerTruckInputPayload(
                    throttle,steer,drum,lights,horn,reverse,yaw,tilt));
            keepalive=3;
            lastThrottle=throttle;
            lastSteer=steer;
            lastYaw=yaw;
            lastTilt=tilt;
        }
    }
}
