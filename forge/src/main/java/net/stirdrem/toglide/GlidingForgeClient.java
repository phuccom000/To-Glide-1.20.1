package net.stirdrem.toglide;

import net.minecraft.client.renderer.item.ItemProperties;
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
                        new net.minecraft.resources.ResourceLocation(ToGlide.MOD_ID),
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

                // Register the 3D first-person model (used in third-person when gliding)
                event.register(new ResourceLocation(
                        id.getNamespace(),
                        id.getPath() + "_3d_first_person"
                ));

                // Register the normal inventory model
                event.register(new ResourceLocation(
                        id.getNamespace(),
                        id.getPath()
                ));

                // Optional: Register a folded version for inventory display
                event.register(new ResourceLocation(
                        id.getNamespace(),
                        id.getPath() + "_folded"
                ));

                // Optional: Register an open version for gliding animation
                event.register(new ResourceLocation(
                        id.getNamespace(),
                        id.getPath() + "_open"
                ));
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
            event.register(new ResourceLocation("stoneycore", gliderType + "_3d_first_person"));

            // Register normal model
            event.register(new ResourceLocation("stoneycore", gliderType));

            // Register variants if needed
            event.register(new ResourceLocation("stoneycore", gliderType + "_folded"));
            event.register(new ResourceLocation("stoneycore", gliderType + "_open"));
        }
    }
}