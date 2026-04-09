package net.stirdrem.toglide.items;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.stirdrem.toglide.ToGlide;

public interface ModItemsRegistry {

    // registers glider items
    Item WOODEN_GLIDER = register("wooden_glider", new FabricGliderItem(-0.26, 1.01, new Item.Properties().stacksTo(1)));
    Item STONE_GLIDER = register("stone_glider", new FabricGliderItem(-0.22, 1.02, new Item.Properties().stacksTo(1)));
    Item IRON_GLIDER = register("iron_glider", new FabricGliderItem(-0.18, 1.03, new Item.Properties().stacksTo(1)));
    Item GOLDEN_GLIDER = register("golden_glider", new FabricGliderItem(-0.16, 1.04, new Item.Properties().stacksTo(1)));
    Item DIAMOND_GLIDER = register("diamond_glider", new FabricGliderItem(-0.14, 1.05, new Item.Properties().stacksTo(1)));
    Item NETHERITE_GLIDER = register("netherite_glider", new FabricGliderItem(-0.12, 1.06, new Item.Properties().stacksTo(1).fireResistant()));

    static void initialize() {
        // get the event for modifying entries in the tools group and register an event handler that adds the mod items.
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register((itemGroup) -> {
            itemGroup.accept(ModItemsRegistry.WOODEN_GLIDER);
            itemGroup.accept(ModItemsRegistry.STONE_GLIDER);
            itemGroup.accept(ModItemsRegistry.IRON_GLIDER);
            itemGroup.accept(ModItemsRegistry.GOLDEN_GLIDER);
            itemGroup.accept(ModItemsRegistry.DIAMOND_GLIDER);
            itemGroup.accept(ModItemsRegistry.NETHERITE_GLIDER);
        });
    }

    // helper method for registering new mod items
    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(ToGlide.MOD_ID, name), item);
    }

    static void registerItems() {
        initialize();
        ToGlide.LOG.info("Registering Mod Items for " + ToGlide.MOD_ID);
    }
}
