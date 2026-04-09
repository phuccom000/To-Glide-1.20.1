package net.stirdrem.toglide.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.items.GliderItem;
import net.stirdrem.toglide.util.GliderUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {

    @Unique
    private float prevArmPitch = 0;

    @Unique
    private float lastLegSwing = 0;

    @Unique
    private long lastUpdateTime = 0;

    @Shadow
    @Final
    public ModelPart leftArm;
    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(at = @At("TAIL"), method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V")
    private void setLimbsGliding(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (livingEntity instanceof Player player) {
            PlayerEntityDuck duck = (PlayerEntityDuck) player;

            // Check if game is paused
            Minecraft mc = Minecraft.getInstance();
            boolean isGamePaused = mc.isPaused();

            if (duck.toglide$isActivatingGlider()) {
                // Handle arm positions for gliding - STRAIGHT UP (no angle)
                if (player.getOffhandItem().isEmpty() || player.getOffhandItem().getItem() instanceof GliderItem) {
                    leftArm.xRot = (float) Math.PI;  // Arm straight up
                    leftArm.zRot = 0;                 // No sideways angle
                }

                if (player.getMainHandItem().isEmpty() || player.getMainHandItem().getItem() instanceof GliderItem) {
                    rightArm.xRot = (float) Math.PI; // Arm straight up
                    rightArm.zRot = 0;                // No sideways angle
                }

                if (duck.toglide$isGliding() && !player.onGround()) {
                    if (isGamePaused) {
                        return;
                    }
                    
                    if ((!player.getMainHandItem().isEmpty() && !GliderUtil.mainHandHoldingGlider(player)) ||
                            (!player.getOffhandItem().isEmpty() && !GliderUtil.offHandHoldingGlider(player))) {
                        ModelPart armHoldingOtherItem = GliderUtil.mainHandHoldingGlider(player) ? leftArm : rightArm;

                        if ((prevArmPitch == (float) -Math.PI / 16 || Math.abs(armHoldingOtherItem.xRot) < (float) Math.PI / 16) && !player.isUsingItem())
                            armHoldingOtherItem.xRot = (float) -Math.PI / 16;

                        prevArmPitch = armHoldingOtherItem.xRot;
                    }
                }
            }
        }
    }

    @Unique
    private float smoothRotation(float current, float previous) {
        // Smooth out rapid changes to prevent jitter
        float maxChange = 0.15f;
        float diff = current - previous;
        if (Math.abs(diff) > maxChange) {
            return previous + (diff > 0 ? maxChange : -maxChange);
        }
        return current;
    }
}