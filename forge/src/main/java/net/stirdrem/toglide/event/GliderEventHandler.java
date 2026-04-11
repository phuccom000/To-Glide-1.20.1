package net.stirdrem.toglide.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
     * Alternative: Using tick event to check if player is on ground
     * More reliable for detecting ground contact
     */
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            Player player = event.player;
            PlayerEntityDuck duck = (PlayerEntityDuck) player;

            // Check if player is gliding and is on ground
            if (duck.toglide$isGliding()) {
                if ((player.onGround() || player.isInWater() || player.isFallFlying())) {
                    duck.toglide$setIsGliding(false);
                    duck.toglide$setActiveGlider(null);
                    duck.toglide$setIsActivatingGlider(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof ServerPlayer target)) return;
        if (!(event.getEntity() instanceof ServerPlayer watcher)) return;

        PlayerEntityDuck duck = (PlayerEntityDuck) target;

        String gliderId = duck.toglide$getActiveGlider() != null
                ? BuiltInRegistries.ITEM.getKey(duck.toglide$getActiveGlider()).toString()
                : "";

        SyncGliderPacket packet = new SyncGliderPacket(
                target.getId(),
                duck.toglide$isGliding(),
                duck.toglide$isActivatingGlider(),
                gliderId
        );

        ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> watcher),
                packet
        );
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer joiningPlayer) {

            // 1. Send ALL existing players → joining player
            for (ServerPlayer other : joiningPlayer.server.getPlayerList().getPlayers()) {
                if (other == joiningPlayer) continue;

                PlayerEntityDuck duck = (PlayerEntityDuck) other;

                SyncGliderPacket packet = new SyncGliderPacket(
                        other.getId(),
                        duck.toglide$isGliding(),
                        duck.toglide$isActivatingGlider(),
                        duck.toglide$getActiveGlider() != null
                                ? BuiltInRegistries.ITEM.getKey(duck.toglide$getActiveGlider()).toString()
                                : ""
                );

                ModNetworking.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> joiningPlayer),
                        packet
                );
            }

            // 2. Send joining player → others
            PlayerEntityDuck duck = (PlayerEntityDuck) joiningPlayer;
            sendSyncPacket(joiningPlayer, duck);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Sync after respawn as well
            if (serverPlayer instanceof PlayerEntityDuck duck) {
                // Optionally reset gliding on respawn
                duck.toglide$setIsGliding(false);
                sendSyncPacket(serverPlayer, duck);
            }
        }
    }

    private static void sendSyncPacket(ServerPlayer player, PlayerEntityDuck duck) {
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

        syncGliderPacket(packet, player);
    }

    protected static void syncGliderPacket(SyncGliderPacket packet, ServerPlayer sp) {
        ModNetworking.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> sp),
                packet
        );

        ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> sp),
                packet
        );
    }
}