package com.piotrek.groundworksconcrete.client.pump;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.pump.ConcretePumpTruckEntity;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Locale;

/** Driver HUD with explicit support/safety and five hydraulic joint states. */
public final class ConcretePumpHud implements HudElement {
    public static void register(){
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(
                GroundworksConcreteMod.MOD_ID,"pump_hud"),new ConcretePumpHud());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g,DeltaTracker dt){
        Minecraft client=Minecraft.getInstance();
        if(client.player==null ||
                !(client.player.getVehicle() instanceof ConcretePumpTruckEntity pump))return;

        int x=8,y=8,w=306,h=128;
        g.fill(x-4,y-4,x+w,y+h,0xB00F2024);
        g.outline(x-4,y-4,w+4,h+4,0xFF20D0D0);
        g.text(client.font,Component.literal("GROUNDWORKS / CONCRETE PUMP"),x,y,
                0xFF42E4D4,true);
        String safety=pump.isRigSupported()?"HYDRAULICS READY"
                :pump.outriggersRequested()?"WAIT FOR SUPPORT":"LEGS RETRACTED";
        g.text(client.font,Component.literal(safety),x,y+13,
                pump.isRigSupported()?0xFF70FF70:0xFFFFB45A,true);
        g.text(client.font,Component.literal(
                String.format(Locale.ROOT,"HOPPER: %.2f / 2.00 m3  %s",
                        pump.displayedHopperUnits()/512.0D,pump.hopperGrade())),
                x,y+26,0xFFE6E6E6,true);
        g.text(client.font,Component.literal(
                String.format(Locale.ROOT,"SWIVEL: %+.1f deg   SELECTED JOINT: %d",
                        pump.slewAngle(),pump.selectedJoint()+1)),
                x,y+40,0xFFDFDFDF,true);
        String joints=String.format(Locale.ROOT,"BOOM: %+.0f | %+.0f | %+.0f | %+.0f | %+.0f",
                pump.jointAngle(0),pump.jointAngle(1),pump.jointAngle(2),
                pump.jointAngle(3),pump.jointAngle(4));
        g.text(client.font,Component.literal(joints),x,y+54,0xFFDFDFDF,true);
        g.text(client.font,Component.literal("P LEGS  J SELECT JOINT  K STOW BOOM"),
                x,y+69,0xFFC9DADE,true);
        g.text(client.font,Component.literal("ARROWS HYDRAULICS  WASD DRIVE"),
                x,y+82,0xFFC9DADE,true);
        g.text(client.font,Component.literal(String.format(Locale.ROOT,
                "PUMP: %s  RATE: %d units/tick  FLOW: %s",
                pump.emergencyStopped()?"E-STOP":pump.pumpEnabled()?"RUN":"OFF",
                pump.pumpRate(),pump.activelyFlowing()?"YES":"NO")),
                x,y+95,0xFFDDDDDD,true);
        g.text(client.font,Component.literal("REMOTE: pair handheld controller for pumping"),
                x,y+108,0xFFC9DADE,true);
    }
}
