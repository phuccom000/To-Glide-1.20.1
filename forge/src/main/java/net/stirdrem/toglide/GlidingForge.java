package net.stirdrem.toglide;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.stirdrem.toglide.items.ModItemsRegistry;
import net.stirdrem.toglide.networking.ModNetworking;

@Mod(ToGlide.MOD_ID)
public class GlidingForge {

    public GlidingForge() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
        ModItemsRegistry.register(modEventBus);
        ModNetworking.register();

        // Use Forge to bootstrap the Common mod.
        ToGlide.LOG.info("Hello Forge world!");
        CommonClass.init();

    }
}