package net.stirdrem.toglide.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.toglide.items.GliderItem;

/*
A bunch of utility methods for using gliders.
 */

public class GliderUtil {

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
        Vec3 currentVelocity = player.getDeltaMovement();  // getVelocity() -> getDeltaMovement()
        double newYVelocity = gliderInHand.glideDropVelocity;

        // only clamp the y velocity if the player was falling faster than the set glider velocity
        if (currentVelocity.y > gliderInHand.glideDropVelocity)
            newYVelocity = currentVelocity.y;

        player.setDeltaMovement(  // setVelocity() -> setDeltaMovement()
                currentVelocity.x * gliderInHand.glideSpeedIncreaseFactor,
                newYVelocity,
                currentVelocity.z * gliderInHand.glideSpeedIncreaseFactor
        );
    }

    public static void resetFallDamage(Player player) {
        player.fallDistance = 0;
    }

}