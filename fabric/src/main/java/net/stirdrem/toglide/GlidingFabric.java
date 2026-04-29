package net.stirdrem.toglide;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.stirdrem.toglide.config.ServerConfig;
import net.stirdrem.toglide.datapack.FabricUpdraftManager;
import net.stirdrem.toglide.event.GliderEventHandler;
import net.stirdrem.toglide.items.ModItemsRegistry;
import net.stirdrem.toglide.sounds.ModSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlidingFabric implements ModInitializer {

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(ToGlide.MOD_ID);

    @Override
    public void onInitialize() {
        ModSounds.registerSounds();
        ModItemsRegistry.registerItems();
        GliderEventHandler.register();
        ServerConfig.HANDLER.load();
        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new FabricUpdraftManager());
    }
}