package net.stirdrem.toglide.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.networking.SyncGliderPacket;

public class GliderItem extends Item implements DyeableLeatherItem {

    public double glideDropVelocity;
    public double glideSpeedIncreaseFactor;
    private static final double MIN_GLIDE_HEIGHT = 1.5;

    public GliderItem(double dropVelocity, double speedFactor, Properties settings) {
        super(settings);

        glideDropVelocity = dropVelocity;
        glideSpeedIncreaseFactor = speedFactor;
    }

    @Override
    public int getColor(ItemStack stack) {
        if (DyeableLeatherItem.super.hasCustomColor(stack)) {
            return DyeableLeatherItem.super.getColor(stack);
        }

        return 0xFFFFFF;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof PlayerEntityDuck duck)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.fail(stack);

        if (!player.onGround() && !player.isFallFlying() && !player.isInWater()) {


            if (!level.isClientSide) {

                // Toggle state
                if (duck.toglide$isGliding()) {
                    duck.toglide$setIsGliding(false);
                    duck.toglide$setActiveGlider(null);
                    duck.toglide$setIsActivatingGlider(false);

                    // Play close sound on server for all players nearby
                    playGliderSound(player, false);
                } else {
                    if (player.getCooldowns().isOnCooldown(this)) {
                        return InteractionResultHolder.fail(stack);
                    }
                    // Check if player is high enough above ground
                    if (!isAboveGround(player, MIN_GLIDE_HEIGHT)) {
                        return InteractionResultHolder.fail(stack);
                    }

                    duck.toglide$setIsGliding(true);
                    duck.toglide$setIsActivatingGlider(true);
                    duck.toglide$setActiveGlider(this);

                    // Play open sound on server for all players nearby
                    playGliderSound(player, true);
                }

                String id = "";
                if (duck.toglide$getActiveGlider() != null) {
                    id = net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .getKey(duck.toglide$getActiveGlider())
                            .toString();
                }

                SyncGliderPacket packet = new SyncGliderPacket(
                        player.getId(),
                        duck.toglide$isGliding(),
                        duck.toglide$isActivatingGlider(),
                        id
                );

                ServerPlayer sp = (ServerPlayer) player;
                syncGliderPacket(packet, sp);
            } else {
                // Client-side sound (immediate feedback)
                boolean isOpening = !duck.toglide$isGliding();
                playGliderSound(player, isOpening);
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }

    /**
     * Play glider open/close sound
     *
     * @param player    The player using the glider
     * @param isOpening true for open sound, false for close sound
     */
    protected void playGliderSound(Player player, boolean isOpening) {
    }

    protected void syncGliderPacket(SyncGliderPacket packet, ServerPlayer sp) {
    }

    /**
     * Checks if the player is at least minHeight blocks above the ground
     *
     * @param player    The player to check
     * @param minHeight The minimum height required (in blocks)
     * @return true if player is above minHeight blocks from ground
     */
    private boolean isAboveGround(Player player, double minHeight) {
        Level level = player.level();
        Vec3 position = player.position();

        // Simple raycast to find the ground
        var hitResult = level.clip(new net.minecraft.world.level.ClipContext(
                position,
                position.subtract(0, position.y + 10, 0),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        ));

        if (hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            double distanceToGround = position.y - hitResult.getLocation().y;
            return distanceToGround >= minHeight;
        }

        return false;
    }
}