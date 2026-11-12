package com.piotrek.groundworksconcrete.pump;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PumpInverseKinematicsTest {
    @Test void solvesReachableVerticalPlaneTipTarget(){
        PumpBoomRig rig=new PumpBoomRig();
        var anchor=rig.pose(0,64,0,0).segments().get(0).start();
        var target=new PumpBoomRig.Point(0,anchor.y()+4,-37);
        assertTrue(rig.aimAt(target,0,64,0,0));
        var estimate=PumpBoomRig.pose(0,64,0,0,rig.targets(),rig.targetSlew()).tip();
        assertTrue(estimate.distance(target)<1.5D);
    }

    @Test void neverCommitsUnreachableTarget(){
        PumpBoomRig rig=new PumpBoomRig();
        var targets=rig.targets();
        assertFalse(rig.aimAt(new PumpBoomRig.Point(1000,1000,1000),0,64,0,0));
        assertArrayEquals(targets,rig.targets());
    }

    @Test void rejectsNonFiniteCoordinates(){
        PumpBoomRig rig=new PumpBoomRig();
        assertFalse(rig.aimAt(new PumpBoomRig.Point(Double.NaN,0,0),0,64,0,0));
    }
}
