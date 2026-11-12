package com.piotrek.groundworksconcrete.pump;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PumpRemoteSafetyTest {
    @Test void pairedOwnerWithinRangeCanOperate() {
        UUID owner=UUID.randomUUID();
        assertTrue(PumpRemoteSafety.mayOperate(owner,owner,true,true,95D*95D));
        assertTrue(PumpRemoteSafety.mayOperate(owner,owner,true,true,96D*96D));
        assertFalse(PumpRemoteSafety.mayOperate(owner,owner,true,true,97D*97D));
    }

    @Test void rejectsOtherPlayersDimensionAndNoRemote() {
        UUID owner=UUID.randomUUID(),other=UUID.randomUUID();
        assertFalse(PumpRemoteSafety.mayOperate(owner,other,true,true,0));
        assertFalse(PumpRemoteSafety.mayOperate(owner,owner,false,true,0));
        assertFalse(PumpRemoteSafety.mayOperate(owner,owner,true,false,0));
        assertFalse(PumpRemoteSafety.mayOperate(null,owner,true,true,0));
        assertFalse(PumpRemoteSafety.mayOperate(owner,owner,true,true,Double.NaN));
    }
}
