package com.piotrek.groundworksconcrete.client;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.client.input.MixerTruckInputHandler;
import com.piotrek.groundworksconcrete.client.input.MixerTruckKeys;
import com.piotrek.groundworksconcrete.client.model.MixerTruckModel;
import com.piotrek.groundworksconcrete.client.render.MixerTruckHud;
import com.piotrek.groundworksconcrete.client.render.MixerTruckRenderer;
import com.piotrek.groundworksconcrete.vehicle.ConcreteTruckRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class GroundworksConcreteClient implements ClientModInitializer {
    public static final ModelLayerLocation MIXER_TRUCK_LAYER=new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID,"mixer_truck"),"main");

    @Override
    public void onInitializeClient(){
        ModelLayerRegistry.registerModelLayer(MIXER_TRUCK_LAYER,MixerTruckModel::createBodyLayer);
        EntityRendererRegistry.register(ConcreteTruckRegistry.MIXER_TRUCK,MixerTruckRenderer::new);
        MixerTruckKeys.register();
        ClientTickEvents.END_CLIENT_TICK.register(MixerTruckInputHandler::tick);
        MixerTruckHud.register();
    }
}
