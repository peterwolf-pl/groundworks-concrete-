package com.piotrek.groundworksconcrete;

import com.piotrek.groundworksconcrete.block.ConcreteBlocks;
import com.piotrek.groundworksconcrete.block.ConcreteBlockEntities;
import com.piotrek.groundworksconcrete.command.ConcreteCommands;
import com.piotrek.groundworksconcrete.command.MixerTruckCommands;
import com.piotrek.groundworksconcrete.item.ConcreteItems;
import com.piotrek.groundworksconcrete.network.MixerTruckInputPayload;
import com.piotrek.groundworksconcrete.vehicle.ConcreteTruckRegistry;
import com.piotrek.groundworksconcrete.vehicle.MixerTruckEntity;
import com.piotrek.groundworksconcrete.world.ConcreteFlowWorld;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Initialises all stages, including truck entity and authenticated controls. */
public final class GroundworksConcreteMod implements ModInitializer {
    public static final String MOD_ID = "pw_groundworks_concrete";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ConcreteBlocks.register();
        ConcreteBlockEntities.register();

        // Register the vehicle before its creative-tab item is referenced.
        ConcreteTruckRegistry.register();
        ConcreteItems.register();

        PayloadTypeRegistry.serverboundPlay().register(
                MixerTruckInputPayload.TYPE, MixerTruckInputPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                MixerTruckInputPayload.TYPE, (payload, context) ->
                    context.server().execute(() -> {
                        if (context.player().getVehicle() instanceof MixerTruckEntity truck) {
                            truck.acceptControls(context.player(),
                                    payload.throttle(), payload.steer(),
                                    payload.toggleDrum(), payload.toggleLights(),
                                    payload.horn(), payload.reverseDrum(),
                                    payload.chuteYaw(), payload.chuteTilt());
                        }
                    }));

        CommandRegistrationCallback.EVENT.register(ConcreteCommands::register);
        CommandRegistrationCallback.EVENT.register(MixerTruckCommands::register);
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getAllLevels().forEach(level -> ConcreteFlowWorld.get(level).tick(level)));

        LOGGER.info("Groundworks Concrete Stage 5: truck, reversible drum, chute and concrete unloading");
    }
}
