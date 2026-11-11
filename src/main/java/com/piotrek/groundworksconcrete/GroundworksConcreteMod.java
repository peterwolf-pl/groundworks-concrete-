package com.piotrek.groundworksconcrete;

import com.piotrek.groundworksconcrete.command.ConcreteCommands;
import com.piotrek.groundworksconcrete.item.ConcreteItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Stage 1: item registration, concrete recipes and exact batch accounting. */
public final class GroundworksConcreteMod implements ModInitializer {
    public static final String MOD_ID = "pw_groundworks_concrete";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ConcreteItems.register();
        CommandRegistrationCallback.EVENT.register(ConcreteCommands::register);
        LOGGER.info("Groundworks Concrete Stage 1 ready: B10, B20, B30");
    }
}
