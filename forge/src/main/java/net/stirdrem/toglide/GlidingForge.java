package net.stirdrem.toglide;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    }
}