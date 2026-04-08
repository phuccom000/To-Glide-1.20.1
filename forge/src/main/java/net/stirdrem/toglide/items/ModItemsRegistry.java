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
            () -> new GliderItem(-0.26, 1.01, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STONE_GLIDER = ITEMS.register("stone_glider",
            () -> new GliderItem(-0.22, 1.02, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> IRON_GLIDER = ITEMS.register("iron_glider",
            () -> new GliderItem(-0.18, 1.03, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GOLDEN_GLIDER = ITEMS.register("golden_glider",
            () -> new GliderItem(-0.16, 1.04, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DIAMOND_GLIDER = ITEMS.register("diamond_glider",
            () -> new GliderItem(-0.14, 1.05, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> NETHERITE_GLIDER = ITEMS.register("netherite_glider",
            () -> new GliderItem(-0.12, 1.06,
                    new Item.Properties().stacksTo(1).fireResistant()));

    private static void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(WOODEN_GLIDER.get());
            event.accept(STONE_GLIDER.get());
            event.accept(IRON_GLIDER.get());
            event.accept(GOLDEN_GLIDER.get());
            event.accept(NETHERITE_GLIDER.get());
        }
    }

    // Call this from your main mod constructor to register everything
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        eventBus.addListener(ModItemsRegistry::addItemsToCreativeTabs);
        ToGlide.LOG.info("Registering Mod Items for " + ToGlide.MOD_ID);
    }
}