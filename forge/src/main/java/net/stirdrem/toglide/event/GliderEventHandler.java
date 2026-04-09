package net.stirdrem.toglide.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.ToGlide;

@Mod.EventBusSubscriber(modid = ToGlide.MOD_ID)
public class GliderEventHandler {

    /**
     * Handle when player lands on ground after falling/gliding
     */
    @SubscribeEvent
    public static void onPlayerLand(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerEntityDuck duck = (PlayerEntityDuck) player;

            // If player was gliding and now hits the ground
            if (duck.toglide$isGliding()) {
                // Stop gliding when hitting the ground
                duck.toglide$setIsGliding(false);
                duck.toglide$setActiveGlider(null);

                // Optional: Reduce fall damage if landing while gliding
                // event.setDamageMultiplier(0.5f);
            }
        }
    }

    /**
     * Alternative: Using tick event to check if player is on ground
     * More reliable for detecting ground contact
     */
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            Player player = event.player;
            PlayerEntityDuck duck = (PlayerEntityDuck) player;

            // Check if player is gliding and is on ground
            if (duck.toglide$isGliding() && (player.onGround() || player.isInWater() || player.isFallFlying())) {
                duck.toglide$setIsGliding(false);
                duck.toglide$setActiveGlider(null);
            }
        }
    }
}