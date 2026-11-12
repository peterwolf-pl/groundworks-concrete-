package com.piotrek.groundworksconcrete.client.render;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.vehicle.MixerTruckEntity;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Locale;

/** Compact driver dashboard with actionable controls and real cargo volume. */
public final class MixerTruckHud implements HudElement {
    public static void register(){
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(
                GroundworksConcreteMod.MOD_ID,"truck_hud"),new MixerTruckHud());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g,DeltaTracker dt){
        Minecraft client=Minecraft.getInstance();
        if(client.player==null || !(client.player.getVehicle() instanceof MixerTruckEntity truck))return;
        int x=9,y=9,w=284,h=85;
        g.fill(x-4,y-4,x+w,y+h,0xA0181D1F);
        g.outline(x-4,y-4,w+4,h+4,0xFFDF8B35);
        g.text(client.font,Component.literal("GROUNDWORKS / CONCRETE MIXER"),x,y,0xFFF6B86D,true);
        String line=String.format(Locale.ROOT,"BATCH: %s  %.2f / 8.00 m3",
                truck.displayedGrade(),truck.displayedUnits()/512F);
        g.text(client.font,Component.literal(line),x,y+13,0xFFEEEEEE,true);
        int filled=Math.round(205F*truck.displayedUnits()/MixerTruckEntity.CAPACITY_UNITS);
        g.fill(x,y+26,x+205,y+32,0xFF444A4E);
        g.fill(x,y+26,x+filled,y+32,0xFF2FBB9D);
        g.text(client.font,Component.literal(String.format(Locale.ROOT,
                "DRUM: %s %.1f RPM   %s    LIGHTS: %s",
                truck.drumSpinning()?(truck.reverseMode()?"REVERSE":"MIX"):"STOP",
                truck.getDrumRpm(),truck.activelyUnloading()?"FLOWING":"READY",
                truck.lightsOn()?"ON":"OFF")),x,y+37,0xFFEEEEEE,true);
        g.text(client.font,Component.literal(String.format(Locale.ROOT,
                "CHUTE: yaw %+.0f deg | down %.0f deg",
                truck.getChuteYaw(),truck.getChuteTilt())),x,y+49,0xFFBEEAD8,true);
        g.text(client.font,Component.literal("WASD DRIVE  R START/STOP  X REVERSE  ARROWS CHUTE"),
                x,y+61,0xFFC9D4D8,true);
        g.text(client.font,Component.literal("L LIGHTS   C HORN   Park to discharge"),
                x,y+73,0xFFC9D4D8,true);
    }
}
