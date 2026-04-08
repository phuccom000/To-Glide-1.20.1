package net.stirdrem.toglide;

import net.fabricmc.api.ModInitializer;
import net.stirdrem.toglide.config.GlidingServersideConfig;
import net.stirdrem.toglide.items.ModItemsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlidingFabric implements ModInitializer {

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(ToGlide.MOD_ID);

    public static GlidingServersideConfig SERVER_CONFIG;

    @Override
    public void onInitialize() {
        ModItemsRegistry.registerItems();
    }
}