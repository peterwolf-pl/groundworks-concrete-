package com.piotrek.groundworksconcrete.pump;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PumpOutriggersTest {
    @Test void deploymentRequiresAllFourSupportedFeet(){
        PumpOutriggers legs=new PumpOutriggers();
        assertFalse(legs.locked(new boolean[]{true,true,true,true}));
        legs.requestDeploy(true);
        for(int i=0;i<41;i++)legs.tick();
        assertTrue(legs.fullyExtended());
        assertTrue(legs.locked(new boolean[]{true,true,true,true}));
        assertFalse(legs.locked(new boolean[]{true,true,false,true}));
        legs.requestDeploy(false);
        for(int i=0;i<41;i++)legs.tick();
        assertTrue(legs.fullyRetracted());
        assertFalse(legs.locked(new boolean[]{true,true,true,true}));
    }

    @Test void feetSwingOutAsLegsExtend(){
        PumpOutriggers legs=new PumpOutriggers();
        var foot0=legs.foot(0,0,64,0,0);
        legs.requestDeploy(true);
        for(int i=0;i<40;i++)legs.tick();
        var foot1=legs.foot(0,0,64,0,0);
        assertTrue(foot1.x()<foot0.x());
        assertEquals(foot0.z(),foot1.z(),0.0001);
        assertThrows(IllegalArgumentException.class,()->legs.extension(4));
    }

    @Test void saveRestoreRejectsInvalidState(){
        PumpOutriggers legs=new PumpOutriggers();
        legs.restore(true,new double[]{1,1,1,1});
        assertTrue(legs.locked(new boolean[]{true,true,true,true}));
        assertThrows(IllegalArgumentException.class,
                ()->legs.restore(true,new double[]{1,1,1,2}));
    }
}
