package net.stirdrem.toglide.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.items.GliderItem;
import net.stirdrem.toglide.networking.SyncGliderPacket;
import net.stirdrem.toglide.platform.Services;
import net.stirdrem.toglide.util.GliderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private int toglide$loginTicks = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // Track ticks since login
        if (player.tickCount < 100) {
            toglide$loginTicks++;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void gliderTick(CallbackInfo ci) {
        try {
            Player player = (Player) (Object) this;
            PlayerEntityDuck duck = (PlayerEntityDuck) this;

            // CRITICAL: On first few ticks after login, re-activate gliding if needed
            if (toglide$loginTicks < 20 && duck.toglide$isGliding() && !player.onGround()) {
                // Make sure glider is in hand
                GliderItem activeGlider = duck.toglide$getActiveGlider();
                if (activeGlider != null) {
                    boolean hasGlider = (player.getMainHandItem().getItem() == activeGlider) ||
                            (player.getOffhandItem().getItem() == activeGlider);

                    if (hasGlider && !player.isFallFlying()) {
                        // Force gliding activation
                        duck.toglide$setIsActivatingGlider(true);
                        GliderUtil.playerGliderMovement(player);
                        GliderUtil.resetFallDamage(player);
                    }
                }
            }

            // Stop gliding when on ground or elytra flying
            if (player.onGround() || player.isFallFlying() || player.isInWater()) {
                if (duck.toglide$isGliding()) {
                    duck.toglide$setIsGliding(false);
                    duck.toglide$setActiveGlider(null);
                    duck.toglide$setIsActivatingGlider(false);

                    if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                        syncToTracking(serverPlayer);
                    }
                }
                return;
            }

            // Only apply glide if currently gliding
            if (duck.toglide$isGliding()) {

                // Stop gliding if active glider is no longer in either hand
                GliderItem activeGlider = duck.toglide$getActiveGlider();
                boolean hasActiveGlider = activeGlider != null &&
                        ((player.getMainHandItem().getItem() == activeGlider) ||
                                (player.getOffhandItem().getItem() == activeGlider));

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
        } catch (Exception e) {
            System.err.println("Error in glider tick: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveGliderData(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        PlayerEntityDuck duck = this;

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

        boolean wasGliding = tag.getBoolean("toglide_isGliding");
        boolean wasActivating = tag.getBoolean("toglide_isActivating");

        duck.toglide$setIsGliding(wasGliding);
        duck.toglide$setIsActivatingGlider(wasActivating);

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
            duck.toglide$setIsActivatingGlider(false);
        }

        // CRITICAL: If we were gliding and are in air, re-apply falling velocity
        if (duck.toglide$isGliding() && !player.onGround() && !player.level().isClientSide) {
            // Give the player a slight downward velocity to trigger gliding
            // This ensures the glider movement starts working immediately
            if (player.getDeltaMovement().y > -0.5) {
                player.setDeltaMovement(player.getDeltaMovement().x, -0.5, player.getDeltaMovement().z);
            }
        }
    }

    // Add this method to sync state to client
    @Unique
    private void syncToTracking(ServerPlayer player) {
        PlayerEntityDuck duck = (PlayerEntityDuck) this;

        String gliderId = "";
        if (duck.toglide$getActiveGlider() != null) {
            gliderId = BuiltInRegistries.ITEM
                    .getKey(duck.toglide$getActiveGlider())
                    .toString();
        }

        SyncGliderPacket packet = new SyncGliderPacket(
                player.getId(),
                duck.toglide$isGliding(),
                duck.toglide$isActivatingGlider(),
                gliderId
        );

        // Send to ALL tracking players
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other == player) continue;

            if (other.distanceTo(player) < 128) { // or use tracking system if you have one
                Services.GLIDING_STATE_HELPER.syncToClient(packet, other);
            }
        }

        // Also send to self
        Services.GLIDING_STATE_HELPER.syncToClient(packet, player);
    }
}