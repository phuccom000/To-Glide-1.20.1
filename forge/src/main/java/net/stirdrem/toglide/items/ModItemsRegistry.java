package net.stirdrem.toglide.items;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.toglide.ToGlide;

@EventBusSubscriber(modid = ToGlide.MOD_ID, bus = Bus.MOD)
public class ModItemsRegistry {

    // Forge DeferredRegister for items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ToGlide.MOD_ID);

    // Register glider items
    public static final RegistryObject<Item> WOODEN_GLIDER = ITEMS.register("wooden_glider",
            () -> new ForgeGliderItem(-0.12, 1.06, new Item.Properties().stacksTo(1)));

    private static void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(WOODEN_GLIDER.get());
        }
    }

    // Call this from your main mod constructor to register everything
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        eventBus.addListener(ModItemsRegistry::addItemsToCreativeTabs);
        ToGlide.LOG.info("Registering Mod Items for " + ToGlide.MOD_ID);
    }
}