package net.stirdrem.toglide.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.networking.SyncGliderPacket;
import net.stirdrem.toglide.platform.Services;

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

        return parseHexColor(Services.CONFIG_HELPER.getDefaultGliderColor());
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

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        if (player.isCreative()) return;

        if (!(player instanceof PlayerEntityDuck duck)) return;

        // Only damage if this is the active glider
        if (!duck.toglide$isGliding()) return;
        if (duck.toglide$getActiveGlider() != this) return;

        // Optional: only damage if actually falling/gliding
        if (player.onGround() || player.isInWater()) return;

        // Damage every X ticks (avoid destroying instantly)
        if (player.tickCount % 20 == 0) { // every 1 second
            stack.hurtAndBreak(1, player, p -> {
                InteractionHand hand = InteractionHand.MAIN_HAND;
                p.broadcastBreakEvent(hand);
            });
        }
        if (Services.CONFIG_HELPER.getEnableIncreasedLightningHit())
            if (level.isThundering() && level.canSeeSky(player.blockPosition())) {

                // Run once per second instead of every tick
                if (player.tickCount % 20 == 0) {

                    // Base chance (tweak this)
                    double chance = 0.05; // 5% per second

                    // Optional: increase chance with height
                    double heightFactor = Math.min(1.0, player.getY() / 256.0);
                    chance += heightFactor * 0.10; // up to +10%

                    if (level.random.nextDouble() < chance) {

                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                        if (lightning != null) {
                            lightning.moveTo(player.getX(), player.getY(), player.getZ());
                            lightning.setCause(player instanceof ServerPlayer sp ? sp : null);
                            level.addFreshEntity(lightning);
                        }
                    }
                }
            }
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

        Vec3 start = player.position();
        Vec3 end = start.subtract(0, 256, 0); // go far enough down to always hit ground

        var hit = level.clip(new net.minecraft.world.level.ClipContext(
                start,
                end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        ));

        if (hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            double distance = start.y - hit.getLocation().y;
            return distance >= minHeight;
        }

        return false;
    }

    public static int parseHexColor(String hex) {
        if (hex == null) return 0xFFFFFF;

        hex = hex.trim();

        // Support "0xFFFFFF", "#FFFFFF", or "FFFFFF"
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        } else if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF; // fallback (white)
        }
    }
}