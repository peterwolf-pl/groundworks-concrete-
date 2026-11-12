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

/** Live handheld operator panel with flow, safety and distance indicators. */
public final class PumpRemoteHud implements HudElement {
    public static void register(){
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(
                GroundworksConcreteMod.MOD_ID,"pump_remote_hud"),new PumpRemoteHud());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g,DeltaTracker dt){
        Minecraft mc=Minecraft.getInstance();
        if(!PumpRemoteInputHandler.isRemoteHeld(mc)||mc.player.getVehicle()!=null)return;
        int x=8,y=8,w=302,h=105;
        g.fill(x-4,y-4,x+w,y+h,0xB01D2428);
        g.outline(x-4,y-4,w+4,h+4,0xFF27D9CC);
        g.text(mc.font,Component.literal("GROUNDWORKS / WIRELESS PUMP REMOTE"),
                x,y,0xFF42E4D4,true);
        ConcretePumpTruckEntity pump=PumpRemoteInputHandler.nearbyPairedPump(mc);
        if(pump==null){
            g.text(mc.font,Component.literal("NOT PAIRED / OUT OF RANGE"),x,y+17,0xFFFFAA44,true);
            g.text(mc.font,Component.literal("Right-click a pump truck with this remote"),
                    x,y+31,0xFFDDDDDD,true);
            return;
        }
        String status=pump.emergencyStopped()?"EMERGENCY STOP LATCHED"
                :!pump.isRigSupported()?"UNSAFE - DEPLOY FOUR LEGS"
                :pump.pumpEnabled()?"PUMP RUNNING":"READY";
        int statusColor=pump.emergencyStopped()?0xFFFF4444
                :!pump.isRigSupported()?0xFFFFB05D:0xFF80FF80;
        g.text(mc.font,Component.literal(status),x,y+16,statusColor,true);
        g.text(mc.font,Component.literal(String.format(Locale.ROOT,
                "FLOW: %s   RATE %d units/tick   HOPPER %.2f m3",
                pump.activelyFlowing()?"ACTIVE":"IDLE",pump.pumpRate(),
                pump.displayedHopperUnits()/512D)),x,y+30,0xFFE8E8E8,true);
        g.text(mc.font,Component.literal(String.format(Locale.ROOT,
                "DISTANCE %.1f / 96 blocks   JOINT %d",
                Math.sqrt(mc.player.distanceToSqr(pump)),pump.selectedJoint()+1)),
                x,y+44,0xFFE8E8E8,true);
        g.text(mc.font,Component.literal("V PUMP  O/U RATE  B E-STOP  N RESET"),
                x,y+59,0xFFDFE5E6,true);
        g.text(mc.font,Component.literal("G LOOK TARGET  J JOINT  ARROWS BOOM"),
                x,y+72,0xFFDFE5E6,true);
        g.text(mc.font,Component.literal("P LEGS  K STOW   Shift + right-click pump to unpair"),
                x,y+85,0xFFCCD6D8,true);
    }
}
