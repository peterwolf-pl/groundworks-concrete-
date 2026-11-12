package com.piotrek.groundworksconcrete.pump;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PumpBoomRigTest {
    @Test void fiveSectionsMaintainLengthsAndFullReach(){
        PumpBoomRig rig=new PumpBoomRig();
        double[] angles=rig.angles();
        assertEquals(5,angles.length);
        var p=rig.pose(0,64,0,0);
        assertEquals(5,p.segments().size());
        for(int i=0;i<5;i++){
            assertEquals(PumpBoomRig.SECTION_LENGTHS[i],
                    p.segments().get(i).start().distance(p.segments().get(i).end()),1e-8);
        }
        assertEquals(44,PumpBoomRig.MAX_REACH,0.001);
    }

    @Test void independentJointsMoveGraduallyAndValidateAngles(){
        var r=new PumpBoomRig();
        r.requestJoint(1,130);
        r.requestSlew(45);
        assertTrue(r.isMoving());
        assertTrue(r.tick(0,64,0,0,part->false));
        assertTrue(Math.abs(r.angle(1)-170)<1);
        assertTrue(Math.abs(r.slew())<=PumpBoomRig.SLEW_SPEED_DEG_PER_TICK+1e-6);
        for(int i=0;i<500;i++)r.tick(0,64,0,0,part->false);
        assertEquals(130,r.angle(1),0.001);
        assertEquals(45,r.slew(),0.001);
        assertEquals(-5,r.angle(0),0.001);
        assertThrows(IllegalArgumentException.class,()->r.requestJoint(5,10));
        assertThrows(IllegalArgumentException.class,()->r.requestJoint(3,Double.NaN));
    }

    @Test void obstructionFreezesActuatorRatherThanClipThroughWall(){
        var r=new PumpBoomRig();
        r.requestJoint(0,40);
        for(int i=0;i<100;i++)assertFalse(r.tick(0,64,0,0,part->true));
        assertEquals(-5,r.angle(0),0.0001);
        r.tick(0,64,0,0,part->false);
        assertTrue(r.angle(0)>-5);
    }

    @Test void restoresSavedGeometryWithoutTeleporting(){
        var r=new PumpBoomRig();
        r.restore(40,new double[]{20,130,-130,95,-100});
        assertEquals(40,r.slew(),0.001);
        assertEquals(95,r.angle(3),0.001);
        assertFalse(r.isMoving());
        r.stow();
        assertTrue(r.isMoving());
        assertThrows(IllegalArgumentException.class,
                ()->r.restore(0,new double[]{0,Double.NaN,0,0,0}));
    }

    @Test void geometryTracksWorldRotation(){
        var r=new PumpBoomRig();
        var a=r.pose(20,70,20,0);
        var b=r.pose(20,70,20,90);
        assertEquals(a.tip().y(),b.tip().y(),1e-7);
        assertEquals(a.segments().size(),b.segments().size());
    }
}
