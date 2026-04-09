package net.stirdrem.toglide.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.ToGlide;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;

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

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Sync glider state when player joins
            if (serverPlayer instanceof PlayerEntityDuck duck) {
                String gliderId = "";
                if (duck.toglide$getActiveGlider() != null) {
                    gliderId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .getKey(duck.toglide$getActiveGlider())
                            .toString();
                }

                SyncGliderPacket packet = new SyncGliderPacket(
                        serverPlayer.getId(),
                        duck.toglide$isGliding(),
                        duck.toglide$isActivatingGlider(),
                        gliderId
                );

                ModNetworking.CHANNEL.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> serverPlayer),
                        packet
                );

                ModNetworking.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        packet
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Sync after respawn as well
            if (serverPlayer instanceof PlayerEntityDuck duck) {
                // Optionally reset gliding on respawn
                duck.toglide$setIsGliding(false);

                String gliderId = "";
                if (duck.toglide$getActiveGlider() != null) {
                    gliderId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .getKey(duck.toglide$getActiveGlider())
                            .toString();
                }

                SyncGliderPacket packet = new SyncGliderPacket(
                        serverPlayer.getId(),
                        duck.toglide$isGliding(),
                        duck.toglide$isActivatingGlider(),
                        gliderId
                );
                ModNetworking.CHANNEL.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> serverPlayer),
                        packet
                );

                ModNetworking.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        packet
                );
            }
        }
    }
}