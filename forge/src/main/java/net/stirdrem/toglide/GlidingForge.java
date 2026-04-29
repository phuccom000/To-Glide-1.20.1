package net.stirdrem.toglide;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.stirdrem.toglide.config.ServerConfig;
import net.stirdrem.toglide.items.ModItemsRegistry;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.sounds.ModSounds;

@Mod(ToGlide.MOD_ID)
public class GlidingForge {

    public GlidingForge() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModSounds.registerSounds(modEventBus);
        ModItemsRegistry.register(modEventBus);
        ModNetworking.register();

        ToGlide.LOG.info("Hello Forge world!");
        CommonClass.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ServerConfig.SPEC);
    }
}