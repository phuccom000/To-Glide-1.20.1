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
    Item WOODEN_GLIDER = register("wooden_glider", new FabricGliderItem(-0.12, 1.06, new Item.Properties().stacksTo(1)));

    static void initialize() {
        // get the event for modifying entries in the tools group and register an event handler that adds the mod items.
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register((itemGroup) -> {
            itemGroup.accept(ModItemsRegistry.WOODEN_GLIDER);
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
