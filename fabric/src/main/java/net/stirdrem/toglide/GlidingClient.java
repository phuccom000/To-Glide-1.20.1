package net.stirdrem.toglide;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.stirdrem.toglide.items.GliderItem;
import net.stirdrem.toglide.networking.ModNetworking;

public class GlidingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModNetworking.registerClient();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof GliderItem)
                ItemProperties.register(item,
                        new net.minecraft.resources.ResourceLocation("gliding"),
                        (stack, world, entity, seed) -> {
                            if (entity instanceof PlayerEntityDuck duck && duck.toglide$isGliding()) {
                                return 1.0F;
                            }
                            return 0.0F;
                        });
        }
    }
}
