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

    @Shadow
    @Final
    public ModelPart leftArm;
    @Shadow
    @Final
    public ModelPart rightArm;
    @Shadow
    @Final
    public ModelPart leftLeg;
    @Shadow
    @Final
    public ModelPart rightLeg;

    @Inject(at = @At("TAIL"), method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V")
    private void setLimbsGliding(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (livingEntity instanceof Player player) {
            PlayerEntityDuck duck = (PlayerEntityDuck) player;

            // Check if game is paused
            Minecraft mc = Minecraft.getInstance();
            boolean isGamePaused = mc.isPaused();

            if (duck.toglide$isActivatingGlider()) {
                // Handle arm positions for gliding
                if (player.getOffhandItem().isEmpty() || player.getOffhandItem().getItem() instanceof GliderItem) {
                    leftArm.xRot = (float) Math.PI;
                    leftArm.zRot = (float) Math.PI / 16;
                }

                if (player.getMainHandItem().isEmpty() || player.getMainHandItem().getItem() instanceof GliderItem) {
                    rightArm.xRot = (float) Math.PI;
                    rightArm.zRot = (float) -Math.PI / 16;
                }

                if (duck.toglide$isGliding() && !player.onGround()) {
                    // Use ageInTicks for consistent animation across all clients instead of instance variables
                    // This ensures the same animation plays on all clients for the same entity

                    // Calculate leg swing based on movement speed
                    float movementSpeed = (float) Math.abs(player.getDeltaMovement().horizontalDistance());
                    float maxSpeed = 1.0f;
                    float speedFactor = Math.min(movementSpeed / maxSpeed, 1.0f);

                    // Calculate vertical speed for dangling effect
                    float verticalSpeed = (float) player.getDeltaMovement().y;
                    float verticalDangle = Math.min(Math.max(verticalSpeed * 0.15f, -0.2f), 0.2f);

                    // Reset Z rotations to 0 (no sideways movement)
                    leftLeg.zRot = 0;
                    rightLeg.zRot = 0;

                    if (isGamePaused) {
                        // GAME IS PAUSED - don't update animations
                        return;
                    }

                    // Use ageInTicks for hover bob - this is consistent across all clients
                    float hoverOffset = (float) Math.sin(ageInTicks * 0.3f) * 0.08f;

                    // Base dangling position
                    float baseDangleX = 0.15f + hoverOffset;

                    // Use ageInTicks for leg swing - ensures all clients see the same animation
                    // This prevents desync issues when players are pushed
                    float legSwingAngle = (float) Math.sin(ageInTicks * 0.8f) * 1.0f * speedFactor;

                    // Apply leg swinging motion - ONLY X-AXIS (front/back)
                    leftLeg.xRot = legSwingAngle + baseDangleX + verticalDangle;
                    rightLeg.xRot = -legSwingAngle + baseDangleX + verticalDangle;

                    // Add wind effect when moving (blows legs back) - based on actual speed
                    float windEffect = movementSpeed * 0.25f;
                    leftLeg.xRot -= windEffect;
                    rightLeg.xRot -= windEffect;

                    // Clamp leg rotations to reasonable values to prevent freaking out
                    leftLeg.xRot = Math.min(Math.max(leftLeg.xRot, -0.8f), 1.2f);
                    rightLeg.xRot = Math.min(Math.max(rightLeg.xRot, -0.8f), 1.2f);

                    // Handle arms when holding items while gliding
                    if ((!player.getMainHandItem().isEmpty() && !GliderUtil.mainHandHoldingGlider(player)) ||
                            (!player.getOffhandItem().isEmpty() && !GliderUtil.offHandHoldingGlider(player))) {
                        ModelPart armHoldingOtherItem = GliderUtil.mainHandHoldingGlider(player) ? leftArm : rightArm;

                        if ((prevArmPitch == (float) -Math.PI / 16 || Math.abs(armHoldingOtherItem.xRot) < (float) Math.PI / 16) && !player.isUsingItem())
                            armHoldingOtherItem.xRot = (float) -Math.PI / 16;

                        prevArmPitch = armHoldingOtherItem.xRot;
                    }
                } else {
                    prevArmPitch = leftArm.xRot;
                }
            }
        }
    }
}