package com.piotrek.groundworksconcrete.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class MixerTruckRenderState extends EntityRenderState {
    public float yaw;
    public float speed;
    public float steerAngle;
    public float wheelRotation;
    public float drumAngle;
    public boolean drumActive;
    public boolean drumReverse;
    public boolean unloading;
    public float chuteYawDegrees;
    public float chuteTiltDegrees;
    public boolean lightsOn;
    public boolean braking;
    public boolean beaconFlash;
    public int cargoUnits;
}
