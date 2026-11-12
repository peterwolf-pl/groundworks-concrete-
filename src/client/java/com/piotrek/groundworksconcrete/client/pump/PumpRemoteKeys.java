package com.piotrek.groundworksconcrete.client.pump;

import com.mojang.blaze3d.platform.InputConstants;
import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** Wireless handheld controller, separate from seated driver key bindings. */
public final class PumpRemoteKeys {
    private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID,"pump_remote"));
    public static KeyMapping PUMP_TOGGLE,RATE_UP,RATE_DOWN,EMERGENCY,RESET,AIM;
    private PumpRemoteKeys(){}

    public static void register(){
        PUMP_TOGGLE=key("remote_pump_toggle",InputConstants.KEY_V);
        RATE_UP=key("remote_rate_up",InputConstants.KEY_O);
        RATE_DOWN=key("remote_rate_down",InputConstants.KEY_U);
        EMERGENCY=key("remote_emergency",InputConstants.KEY_B);
        RESET=key("remote_reset",InputConstants.KEY_N);
        AIM=key("remote_aim",InputConstants.KEY_G);
    }

    private static KeyMapping key(String name,int keyCode){
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.pw_groundworks_concrete."+name,
                InputConstants.Type.KEYBOARD,keyCode,CATEGORY));
    }
}
