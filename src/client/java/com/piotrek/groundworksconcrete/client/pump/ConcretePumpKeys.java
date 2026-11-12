package com.piotrek.groundworksconcrete.client.pump;

import com.mojang.blaze3d.platform.InputConstants;
import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** In-cab pump truck controls. Stage 7 adds a separate handheld remote. */
public final class ConcretePumpKeys {
    private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID,"pump_controls"));

    public static KeyMapping LEGS,SELECT_NEXT,STOW,LIGHTS,HORN;
    public static KeyMapping JOINT_UP,JOINT_DOWN,SLEW_LEFT,SLEW_RIGHT;
    private ConcretePumpKeys(){}

    public static void register(){
        LEGS=key("pump_legs",InputConstants.KEY_P);
        SELECT_NEXT=key("pump_next_joint",InputConstants.KEY_J);
        STOW=key("pump_stow",InputConstants.KEY_K);
        LIGHTS=key("pump_lights",InputConstants.KEY_L);
        HORN=key("pump_horn",InputConstants.KEY_C);
        JOINT_UP=key("pump_joint_up",InputConstants.KEY_UP);
        JOINT_DOWN=key("pump_joint_down",InputConstants.KEY_DOWN);
        SLEW_LEFT=key("pump_slew_left",InputConstants.KEY_LEFT);
        SLEW_RIGHT=key("pump_slew_right",InputConstants.KEY_RIGHT);
    }

    private static KeyMapping key(String name,int code){
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.pw_groundworks_concrete."+name,InputConstants.Type.KEYBOARD,code,CATEGORY));
    }
}
