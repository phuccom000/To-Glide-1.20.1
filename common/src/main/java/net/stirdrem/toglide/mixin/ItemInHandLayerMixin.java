package net.stirdrem.toglide.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.toglide.PlayerEntityDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.entity.layers.ItemInHandLayer.class)
public class ItemInHandLayerMixin<T extends net.minecraft.world.entity.LivingEntity, M extends net.minecraft.client.model.EntityModel<T>> {

    @Inject(
            method = "renderArmWithItem",
            at = @At("HEAD")
    )
    private void adjustThirdPersonItem(
            LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci
    ) {
        if (!(livingEntity instanceof Player player)) return;
        if (!(player instanceof PlayerEntityDuck duck)) return;

        if (!duck.toglide$isGliding()) return;

        if (!(player instanceof AbstractClientPlayer clientPlayer)) return;

        boolean slim = clientPlayer.getModelName().equals("slim");

        if (!slim) return;

        float offset = 0.03125F;
        if (arm == HumanoidArm.RIGHT) {
            poseStack.translate(-offset, 0.0F, 0.0F);
        } else if (arm == HumanoidArm.LEFT) {
            poseStack.translate(offset * 2, 0.0F, 0.0F);
        }
    }
}