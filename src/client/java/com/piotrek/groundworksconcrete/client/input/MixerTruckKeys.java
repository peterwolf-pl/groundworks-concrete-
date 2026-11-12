package com.piotrek.groundworksconcrete.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** Remappable driver controls, active only while mounted in the mixer truck. */
public final class MixerTruckKeys {
    private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID,"truck_controls"));

    public static KeyMapping DRUM, REVERSE, LIGHTS, HORN;
    public static KeyMapping CHUTE_LEFT, CHUTE_RIGHT, CHUTE_UP, CHUTE_DOWN;
    private MixerTruckKeys(){}

    public static void register(){
        DRUM=key("drum",InputConstants.KEY_R);
        REVERSE=key("reverse",InputConstants.KEY_X);
        LIGHTS=key("lights",InputConstants.KEY_L);
        HORN=key("horn",InputConstants.KEY_C);
        CHUTE_LEFT=key("chute_left",InputConstants.KEY_LEFT);
        CHUTE_RIGHT=key("chute_right",InputConstants.KEY_RIGHT);
        CHUTE_UP=key("chute_up",InputConstants.KEY_UP);
        CHUTE_DOWN=key("chute_down",InputConstants.KEY_DOWN);
    }

    private static KeyMapping key(String name,int code){
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.pw_groundworks_concrete."+name,
                InputConstants.Type.KEYBOARD,code,CATEGORY));
    }
}
