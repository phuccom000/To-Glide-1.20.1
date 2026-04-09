package net.stirdrem.toglide.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stirdrem.toglide.PlayerEntityDuck;

public class GliderItem extends Item {

    public double glideDropVelocity;
    public double glideSpeedIncreaseFactor;

    public GliderItem(double dropVelocity, double speedFactor, Properties settings) {
        super(settings);

        glideDropVelocity = dropVelocity;
        glideSpeedIncreaseFactor = speedFactor;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof PlayerEntityDuck duck)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);

        // Only toggle if in the air and not elytra-flying
        if (!player.onGround() && !player.isFallFlying() && !player.isInWater()) {

            // If already gliding → stop
            if (duck.toglide$isGliding()) {
                duck.toglide$setIsGliding(false);
                duck.toglide$setActiveGlider(null);
                duck.toglide$setIsActivatingGlider(false);
            } else {
                // Start gliding
                duck.toglide$setIsGliding(true);
                duck.toglide$setIsActivatingGlider(true);
                duck.toglide$setActiveGlider(this);
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        } else {
            duck.toglide$setIsGliding(false);
            duck.toglide$setActiveGlider(null);
            duck.toglide$setIsActivatingGlider(false);
        }

        return InteractionResultHolder.pass(stack);
    }
}