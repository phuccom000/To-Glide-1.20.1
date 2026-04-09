package net.stirdrem.toglide.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.toglide.items.GliderItem;

/*
A bunch of utility methods for using gliders.
 */

public class GliderUtil {

    // Thermal updraft strength values
    private static final double CAMPFIRE_UPDRAFT = 0.25;
    private static final double FIRE_UPDRAFT = 0.20;
    private static final double LAVA_UPDRAFT = 0.35;
    private static final double SOUL_CAMPFIRE_UPDRAFT = 0.30;
    private static final double SOUL_FIRE_UPDRAFT = 0.25;

    // Maximum vertical velocity from updrafts
    private static final double MAX_UPDRAFT_VELOCITY = 0.8;

    // Range to check below player (in blocks)
    private static final int CHECK_RANGE = 20;

    public static boolean isHoldingGlider(Player player) {
        return player.isHolding(itemStack -> itemStack.getItem() instanceof GliderItem);
    }

    public static boolean offHandHoldingGlider(Player player) {
        return player.getOffhandItem().getItem() instanceof GliderItem;
    }

    public static boolean mainHandHoldingGlider(Player player) {
        return player.getMainHandItem().getItem() instanceof GliderItem;
    }

    public static GliderItem getGliderItemInHand(Player player) {
        GliderItem gliderItem = null;

        if (mainHandHoldingGlider(player))
            gliderItem = (GliderItem) player.getMainHandItem().getItem();
        else if (offHandHoldingGlider(player))
            gliderItem = (GliderItem) player.getOffhandItem().getItem();

        return gliderItem;
    }

    public static void playerGliderMovement(Player player) {
        GliderItem gliderInHand = GliderUtil.getGliderItemInHand(player);
        Vec3 currentVelocity = player.getDeltaMovement();

        // Start with the default glide drop velocity (usually negative)
        double newYVelocity = gliderInHand.glideDropVelocity;

        // Apply thermal updraft if available
        UpdraftInfo updraftInfo = getThermalUpdraftInfo(player);

        if (updraftInfo.hasUpdraft) {
            double distanceToSource = updraftInfo.distanceToSource;
            double updraftStrength = updraftInfo.strength;

            // Calculate how high above the source the player is
            double heightAboveSource = updraftInfo.heightAboveSource;

            // Calculate target height - we want to keep player within CHECK_RANGE blocks above source
            // The updraft is strongest when close to source and weaker as you go higher
            double targetHeightRange = CHECK_RANGE;

            // Calculate strength multiplier based on height above source
            // When close to source (0-5 blocks): strong upward push
            // When at ideal height (5-15 blocks): gentle lift to maintain height
            // When above ideal height (15+ blocks): weak or no push
            double heightMultiplier;

            if (heightAboveSource < 5) {
                // Very close to source - strong upward push to get into gliding range
                heightMultiplier = 1.5;
            } else if (heightAboveSource < targetHeightRange * 0.7) {
                // Below ideal height - moderate upward push
                heightMultiplier = 1.0;
            } else if (heightAboveSource < targetHeightRange) {
                // At ideal height - gentle lift to maintain position
                heightMultiplier = 0.5;
            } else {
                // Above ideal height - weak push or no lift
                heightMultiplier = 0.2;
            }

            // Calculate final upward force
            double upwardForce = updraftStrength * heightMultiplier;

            // Apply upward force to counteract falling or push up
            newYVelocity = gliderInHand.glideDropVelocity + upwardForce;

            // If player is falling and updraft is strong enough, push up instead of down
            if (currentVelocity.y < 0 && upwardForce > Math.abs(gliderInHand.glideDropVelocity)) {
                newYVelocity = upwardForce * 0.8; // Strong upward push
            }

            // If player is already rising, maintain or increase upward velocity
            if (currentVelocity.y > 0 && heightAboveSource < targetHeightRange) {
                newYVelocity = Math.max(currentVelocity.y + upwardForce * 0.3, newYVelocity);
            }

            // Cap the upward velocity to prevent infinite flight
            newYVelocity = Math.min(newYVelocity, MAX_UPDRAFT_VELOCITY);

            // Ensure player doesn't fall below the updraft source range
            if (heightAboveSource < 2 && currentVelocity.y < 0) {
                // Too close to source - give a strong upward boost
                newYVelocity = Math.max(newYVelocity, 0.3);
            }
        } else {
            // No updraft - only clamp if falling faster than glide drop velocity
            if (currentVelocity.y > gliderInHand.glideDropVelocity) {
                newYVelocity = currentVelocity.y;
            }
        }

        player.setDeltaMovement(
                currentVelocity.x * gliderInHand.glideSpeedIncreaseFactor,
                newYVelocity,
                currentVelocity.z * gliderInHand.glideSpeedIncreaseFactor
        );
    }

    /**
     * Data class for updraft information
     */
    private static class UpdraftInfo {
        boolean hasUpdraft;
        double strength;
        double distanceToSource;
        double heightAboveSource;

        UpdraftInfo(boolean hasUpdraft, double strength, double distanceToSource, double heightAboveSource) {
            this.hasUpdraft = hasUpdraft;
            this.strength = strength;
            this.distanceToSource = distanceToSource;
            this.heightAboveSource = heightAboveSource;
        }
    }

    /**
     * Checks for thermal updraft sources below the player
     *
     * @param player The gliding player
     * @return UpdraftInfo with strength and distance information
     */
    private static UpdraftInfo getThermalUpdraftInfo(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        double playerY = player.getY();

        // Check blocks below the player within range
        for (int y = 1; y <= CHECK_RANGE; y++) {
            BlockPos checkPos = playerPos.below(y);
            BlockState state = level.getBlockState(checkPos);

            double sourceY = checkPos.getY() + 1; // Top of the block
            double heightAboveSource = playerY - sourceY;

            // Calculate base strength based on block type
            double baseStrength = 0;

            // Check for campfires (lit only)
            if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
                if (state.is(Blocks.SOUL_CAMPFIRE)) {
                    baseStrength = SOUL_CAMPFIRE_UPDRAFT;
                } else {
                    baseStrength = CAMPFIRE_UPDRAFT;
                }
            }
            // Check for fire
            else if (state.is(Blocks.FIRE)) {
                baseStrength = FIRE_UPDRAFT;
            }
            // Check for soul fire
            else if (state.is(Blocks.SOUL_FIRE)) {
                baseStrength = SOUL_FIRE_UPDRAFT;
            }
            // Check for lava (any lava block)
            else if (state.is(Blocks.LAVA)) {
                baseStrength = LAVA_UPDRAFT;
            }

            if (baseStrength > 0) {
                // Distance-based falloff (closer = stronger)
                double distanceFalloff = Math.max(0, 1.0 - (y - 1) * 0.04);

                // Height-based falloff (lower height above source = stronger)
                double heightFalloff = Math.max(0, 1.0 - (heightAboveSource / CHECK_RANGE));

                // Combine falloffs
                double finalStrength = baseStrength * distanceFalloff * heightFalloff;

                return new UpdraftInfo(true, finalStrength, y, heightAboveSource);
            }

            // Stop checking if we hit solid ground (prevents checking through walls)
            if (level.getBlockState(checkPos).isSolid() && y <= 5) {
                break;
            }
        }

        return new UpdraftInfo(false, 0, 0, 0);
    }

    /**
     * Checks for thermal updraft sources below the player
     *
     * @param player The gliding player
     * @return Updraft strength (0 if no source found)
     */
    private static double getThermalUpdraftStrength(Player player) {
        return getThermalUpdraftInfo(player).strength;
    }

    /**
     * Check if player is currently receiving a thermal updraft
     */
    public static boolean isReceivingThermalUpdraft(Player player) {
        return getThermalUpdraftInfo(player).hasUpdraft;
    }

    /**
     * Get the current updraft strength for visual/audio effects
     */
    public static double getCurrentUpdraftStrength(Player player) {
        return getThermalUpdraftInfo(player).strength;
    }

    /**
     * Get distance to nearest updraft source
     */
    public static double getDistanceToUpdraftSource(Player player) {
        return getThermalUpdraftInfo(player).distanceToSource;
    }

    /**
     * Get height above updraft source
     */
    public static double getHeightAboveUpdraftSource(Player player) {
        return getThermalUpdraftInfo(player).heightAboveSource;
    }

    public static void resetFallDamage(Player player) {
        player.fallDistance = 0;
    }
}