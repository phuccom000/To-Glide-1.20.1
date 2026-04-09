package net.stirdrem.toglide;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.toglide.items.GliderItem;

@Mod.EventBusSubscriber(modid = ToGlide.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class GlidingForgeClient {

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        // Register glider "gliding" property
        for (Item item : ForgeRegistries.ITEMS) {
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

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Register models for all glider items
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof GliderItem) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                ModelResourceLocation glider3DModel = new ModelResourceLocation(
                        id.getNamespace(),
                        id.getPath() + "_predicate",
                        "inventory"
                );
                // Register the 3D first-person model (used in third-person when gliding)
                event.register(glider3DModel);
                ModelResourceLocation normalModel = new ModelResourceLocation(
                        id.getNamespace(),
                        id.getPath(),
                        "inventory"
                );
                // Register the normal inventory model
                event.register(normalModel);
            }
        }

        // If you want to manually specify glider items instead of detecting automatically
        registerManualGliderModels(event);
    }

    private static void registerManualGliderModels(ModelEvent.RegisterAdditional event) {
        // Example: Manually register specific glider models
        String[] gliderTypes = {
                "wooden_glider",
                "iron_glider",
                "golden_glider",
                "diamond_glider",
                "netherite_glider"
        };

        for (String gliderType : gliderTypes) {
            // Register 3D first-person model
            event.register(new ResourceLocation("stoneycore", gliderType + "_predicate"));

            // Register normal model
            event.register(new ResourceLocation("stoneycore", gliderType));

            // Register variants if needed
            event.register(new ResourceLocation("stoneycore", gliderType + "_folded"));
            event.register(new ResourceLocation("stoneycore", gliderType + "_open"));
        }
    }
}