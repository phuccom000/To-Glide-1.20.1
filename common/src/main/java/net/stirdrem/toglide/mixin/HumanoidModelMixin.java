package net.stirdrem.toglide.mixin;

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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {

    private float prevLegPitch = 0;
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
    private void setLimbsGliding(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (livingEntity instanceof Player player) {
            if (((PlayerEntityDuck) player).toglide$isActivatingGlider()) {
                if (player.getOffhandItem().isEmpty() || player.getOffhandItem().getItem() instanceof GliderItem) {
                    leftArm.xRot = (float) Math.PI;
                    leftArm.zRot = (float) Math.PI / 16;
                }

                if (player.getMainHandItem().isEmpty() || player.getMainHandItem().getItem() instanceof GliderItem) {
                    rightArm.xRot = (float) Math.PI;
                    rightArm.zRot = (float) -Math.PI / 16;
                }

                if (((PlayerEntityDuck) player).toglide$isGliding() && !player.onGround()) {
                    // only set legs straight if they were already straight, otherwise first allow stepping leg swing
                    // animation to finish
                    if (prevLegPitch == 0 || Math.abs(leftLeg.xRot) < (float) Math.PI / 32) {
                        leftLeg.xRot = 0;
                        rightLeg.xRot = 0;
                    }

                    // if either hand is holding an item other than a glider while gliding
                    if ((!player.getMainHandItem().isEmpty() && !GliderUtil.mainHandHoldingGlider(player)) ||
                            (!player.getOffhandItem().isEmpty() && !GliderUtil.offHandHoldingGlider(player))) {
                        ModelPart armHoldingOtherItem = GliderUtil.mainHandHoldingGlider(player) ? leftArm : rightArm;

                        if ((prevArmPitch == (float) -Math.PI / 16 || Math.abs(armHoldingOtherItem.xRot) < (float) Math.PI / 16) && !player.isUsingItem())
                            armHoldingOtherItem.xRot = (float) -Math.PI / 16;

                        prevArmPitch = armHoldingOtherItem.xRot;
                    }
                } else prevArmPitch = leftArm.xRot;

                prevLegPitch = leftLeg.xRot;
            }
        }
    }
}