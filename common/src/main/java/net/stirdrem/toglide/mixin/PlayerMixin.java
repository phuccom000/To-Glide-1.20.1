package net.stirdrem.toglide.mixin;

import net.minecraft.world.entity.player.Player;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.items.GliderItem;
import net.stirdrem.toglide.util.GliderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerEntityDuck {

    private boolean toglide$isGliding;
    private boolean toglide$isActivatingGlider;
    private GliderItem toglide$activeGlider;

    @Override
    public boolean toglide$isGliding() {
        return toglide$isGliding;
    }

    @Override
    public void toglide$setIsGliding(boolean value) {
        this.toglide$isGliding = value;

        if (!value) {
            this.toglide$activeGlider = null;
            this.toglide$isActivatingGlider = false;
        }
    }

    @Override
    public boolean toglide$isActivatingGlider() {
        return toglide$isActivatingGlider;
    }

    @Override
    public void toglide$setIsActivatingGlider(boolean value) {
        this.toglide$isActivatingGlider = value;
    }

    @Override
    public GliderItem toglide$getActiveGlider() {
        return toglide$activeGlider;
    }

    @Override
    public void toglide$setActiveGlider(GliderItem item) {
        this.toglide$activeGlider = item;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void gliderTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        PlayerEntityDuck duck = (PlayerEntityDuck) this;
        
        // Stop gliding when on ground or elytra flying
        if (player.onGround() || player.isFallFlying() || player.isInWater()) {
            duck.toglide$setIsGliding(false);
            return;
        }

        // Only apply glide if currently gliding
        if (duck.toglide$isGliding()) {

            // Stop gliding if active glider is no longer in either hand
            GliderItem activeGlider = duck.toglide$getActiveGlider();
            boolean hasActiveGlider =
                    (player.getMainHandItem().getItem() == activeGlider) ||
                            (player.getOffhandItem().getItem() == activeGlider);

            if (!hasActiveGlider) {
                duck.toglide$setIsGliding(false);
                duck.toglide$setActiveGlider(null);
                duck.toglide$setIsActivatingGlider(false);
                return;
            }

            // Apply glider movement
            GliderUtil.playerGliderMovement(player);
            GliderUtil.resetFallDamage(player);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveGliderData(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        PlayerEntityDuck duck = (PlayerEntityDuck) this;

        tag.putBoolean("toglide_isGliding", duck.toglide$isGliding());
        tag.putBoolean("toglide_isActivating", duck.toglide$isActivatingGlider());

        if (duck.toglide$getActiveGlider() != null) {
            net.minecraft.resources.ResourceLocation id =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(duck.toglide$getActiveGlider());

            tag.putString("toglide_activeGlider", id.toString());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadGliderData(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        PlayerEntityDuck duck = (PlayerEntityDuck) this;
        Player player = (Player) (Object) this;

        duck.toglide$setIsGliding(tag.getBoolean("toglide_isGliding"));
        duck.toglide$setIsActivatingGlider(tag.getBoolean("toglide_isActivating"));

        if (tag.contains("toglide_activeGlider")) {
            net.minecraft.resources.ResourceLocation id =
                    new net.minecraft.resources.ResourceLocation(tag.getString("toglide_activeGlider"));

            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id)) {
                if (net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id) instanceof GliderItem glider) {
                    duck.toglide$setActiveGlider(glider);
                }
            }
        }

        // Safety: don't resume gliding on ground
        if (player.onGround()) {
            duck.toglide$setIsGliding(false);
        }
    }
}