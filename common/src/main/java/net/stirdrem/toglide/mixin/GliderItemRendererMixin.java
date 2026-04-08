package net.stirdrem.toglide.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.items.GliderItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class GliderItemRendererMixin {

    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At("HEAD"),
            cancellable = true)
    public void toglide$renderGliderThirdPerson(LivingEntity entity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean leftHanded, PoseStack poseStack, MultiBufferSource multiBufferSource, Level level, int light, int overlay, int seed, CallbackInfo ci) {
        // Only replace held item model in third person
        if (itemDisplayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            if (entity instanceof PlayerEntityDuck duck && duck.toglide$isGliding()) {
                GliderItem activeGlider = duck.toglide$getActiveGlider();
                if (activeGlider != null && itemStack.getItem() instanceof GliderItem) {
                    BakedModel gliderModel = getGliderThirdPersonModel(itemStack, entity, seed);
                    if (gliderModel != null) {
                        ((ItemRenderer) (Object) this).render(itemStack, itemDisplayContext, leftHanded, poseStack, multiBufferSource, light, overlay, gliderModel);
                        ci.cancel(); // cancel the original render
                    }
                }
            }
        }

    }

    @Inject(method = "render",
            at = @At("HEAD"), cancellable = true)
    public void toglide$renderGliderGUI(ItemStack itemStack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay, BakedModel model, CallbackInfo ci) {
        // Keep GUI rendering as normal - don't change the icon
        if (renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.FIXED) {
            // Check if it's a glider item
            if (itemStack.getItem() instanceof GliderItem) {
                // For GUI, we want to render the normal item model, not the gliding pose
                // So we just return and let the normal rendering happen
                return;
            }
        }

        // Handle third-person glider rendering for non-GUI contexts
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && renderMode != ItemDisplayContext.GUI && renderMode != ItemDisplayContext.FIXED) {
            PlayerEntityDuck duck = (PlayerEntityDuck) client.player;
            if (duck.toglide$isGliding() && duck.toglide$getActiveGlider() != null &&
                    itemStack.getItem() instanceof GliderItem) {

                BakedModel gliderModel = getGliderThirdPersonModel(itemStack, client.player, 0);
                if (gliderModel != null) {
                    gliderModel.getTransforms().getTransform(renderMode).apply(leftHanded, poseStack);
                    poseStack.translate(-0.5F, -0.5F, -0.5F);

                    RenderType renderType = RenderType.cutout();
                    VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);

                    // Render with default colors (no dye)
                    renderBakedItemModel(gliderModel, light, overlay, poseStack, vertexConsumer, new float[]{1.0F, 1.0F, 1.0F});
                    ci.cancel();
                }
            }
        }
    }

    @Unique
    private BakedModel getGliderThirdPersonModel(ItemStack itemStack, LivingEntity entity, int seed) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getItemRenderer().getItemModelShaper().getModelManager();

        // Use a specific model for the glider when in third person
        // This model should show the glider in an open/gliding position
        String itemPath = itemStack.getItemHolder().unwrapKey().get().location().getPath();
        String itemNamespace = itemStack.getItemHolder().unwrapKey().get().location().getNamespace();
        String modelPath = determineModelPath(itemStack);
        ModelResourceLocation iModelPath = new ModelResourceLocation(
                itemNamespace,
                modelPath,
                "inventory"
        );

        BakedModel bakedModel = modelManager.getMissingModel();

        if (!modelPath.isEmpty()) {
            bakedModel = modelManager.getModel(iModelPath);
        }

        if (entity != null && bakedModel != null) {
            ClientLevel clientLevel = entity.level() instanceof ClientLevel ? (ClientLevel) entity.level() : null;
            BakedModel overrideModel = bakedModel.getOverrides().resolve(bakedModel, itemStack, clientLevel, entity, seed);
            if (overrideModel != null) {
                bakedModel = overrideModel;
            }
        }

        return bakedModel;
    }

    private String determineModelPath(ItemStack stack) {
        if (stack.getItem() instanceof GliderItem) return stack.getItem() + "_3d_first_person";
        return stack.getItem().toString();
    }

    @Unique
    private void renderBakedItemModel(BakedModel model, int light, int overlay, PoseStack matrices, VertexConsumer vertices, float[] color) {
        net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            random.setSeed(42L);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, random), light, overlay, color);
        }

        random.setSeed(42L);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, random), light, overlay, color);
    }

    @Unique
    private void renderBakedItemQuads(PoseStack poseStack, VertexConsumer vertices, java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> quads, int light, int overlay, float[] color) {
        PoseStack.Pose pose = poseStack.last();
        for (net.minecraft.client.renderer.block.model.BakedQuad bakedQuad : quads) {
            float r = color[0];
            float g = color[1];
            float b = color[2];
            vertices.putBulkData(pose, bakedQuad, r, g, b, light, overlay);
        }
    }
}