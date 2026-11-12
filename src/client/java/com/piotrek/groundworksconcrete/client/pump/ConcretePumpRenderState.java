package com.piotrek.groundworksconcrete.client.pump;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Snapshot of the five actuators and four hydraulic stabilizers for rendering. */
public final class ConcretePumpRenderState extends EntityRenderState {
    public float yaw;
    public float wheelRotation,steerAngle,forwardSpeed;
    public float boomSlew;
    public final float[] joints=new float[5];
    public final float[] legExtensions=new float[4];
    public boolean deployed,supported,lights,braking,beaconFlash;
    public int hopperUnits,selectedJoint;
}
