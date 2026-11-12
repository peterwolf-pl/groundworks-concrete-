package com.piotrek.groundworksconcrete.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class MixerTruckKeys {
    private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID,"truck_controls"));
    public static KeyMapping DRUM;
    public static KeyMapping LIGHTS;
    public static KeyMapping HORN;
    private MixerTruckKeys(){}

    public static void register(){
        DRUM=key("drum",InputConstants.KEY_R);
        LIGHTS=key("lights",InputConstants.KEY_L);
        HORN=key("horn",InputConstants.KEY_C);
    }

    private static KeyMapping key(String name,int code){
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.pw_groundworks_concrete."+name,
                InputConstants.Type.KEYBOARD,code,CATEGORY));
    }
}
