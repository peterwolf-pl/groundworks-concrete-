package com.piotrek.groundworksconcrete;

import com.piotrek.groundworksconcrete.block.ConcreteBlocks;
import com.piotrek.groundworksconcrete.command.ConcreteCommands;
import com.piotrek.groundworksconcrete.item.ConcreteItems;
import com.piotrek.groundworksconcrete.world.ConcreteFlowWorld;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Stage 2: physically bounded finite-volume flow and timed curing. */
public final class GroundworksConcreteMod implements ModInitializer {
    public static final String MOD_ID = "pw_groundworks_concrete";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ConcreteBlocks.register();
        ConcreteItems.register();
        CommandRegistrationCallback.EVENT.register(ConcreteCommands::register);
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getAllLevels().forEach(level -> ConcreteFlowWorld.get(level).tick(level)));
        LOGGER.info("Groundworks Concrete Stage 2 ready: finite-volume wet concrete and curing");
    }
}
