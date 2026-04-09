package net.stirdrem.toglide.mixin;

import net.stirdrem.toglide.PlayerEntityDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.player.RemotePlayer.class)
public abstract class RemotePlayerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void toglide$updateGlidingVisual(CallbackInfo ci) {
        net.minecraft.client.player.RemotePlayer player = (net.minecraft.client.player.RemotePlayer) (Object) this;

        if (!(player instanceof PlayerEntityDuck duck)) return;

        if (duck.toglide$isGliding()) {

            duck.toglide$setIsActivatingGlider(true);

            // Optional: safety reset if needed
            if (player.onGround() || player.isInWater() || player.isFallFlying()) {
                duck.toglide$setIsGliding(false);
                duck.toglide$setIsActivatingGlider(false);
            }
        }
    }
}