package net.stirdrem.toglide.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.stirdrem.toglide.items.GliderItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Shadow
    protected abstract void loadTopLevel(ModelResourceLocation modelId);

    @Inject(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
                    ordinal = 3, shift = At.Shift.AFTER))
    public void stoneycore$registerGliderModels(BlockColors blockColors, ProfilerFiller profilerFiller,
                                                Map<ResourceLocation, BlockModel> blockModelMap,
                                                Map<ResourceLocation, List<ModelBakery.LoadedJson>> blockStates,
                                                CallbackInfo ci) {

        // Register models for all glider items
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof GliderItem) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);

                // Register the 3D first-person model (used in third-person when gliding)
                ModelResourceLocation glider3DModel = new ModelResourceLocation(
                        id.getNamespace(),
                        id.getPath() + "_3d_first_person",
                        "inventory"
                );
                this.loadTopLevel(glider3DModel);

                // Register the normal inventory model
                ModelResourceLocation normalModel = new ModelResourceLocation(
                        id.getNamespace(),
                        id.getPath(),
                        "inventory"
                );
                this.loadTopLevel(normalModel);

                // Optional: Register a folded version for inventory display
                ModelResourceLocation foldedModel = new ModelResourceLocation(
                        id.getNamespace(),
                        id.getPath() + "_folded",
                        "inventory"
                );
                this.loadTopLevel(foldedModel);

                // Optional: Register an open version for gliding animation
                ModelResourceLocation openModel = new ModelResourceLocation(
                        id.getNamespace(),
                        id.getPath() + "_open",
                        "inventory"
                );
                this.loadTopLevel(openModel);
            }
        }
    }
}