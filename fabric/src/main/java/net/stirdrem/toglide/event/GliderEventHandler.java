package net.stirdrem.toglide.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.ToGlide;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;

public class GliderEventHandler {

    public static void register() {
        // Sync when player joins - AFTER they're fully loaded
        /*
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer joiningPlayer = handler.getPlayer();
            server.execute(() -> {
                server.execute(() -> {

                    ToGlide.LOG.debug("=== PLAYER JOIN: {} ===", joiningPlayer.getName().getString());

                    for (ServerPlayer other : PlayerLookup.tracking(joiningPlayer)) {
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

                        send(packet, joiningPlayer);
                    }

                    // send self state AFTER others
                    PlayerEntityDuck duck = (PlayerEntityDuck) joiningPlayer;
                    sendSyncPacket(joiningPlayer, duck);

                });
            });
        });
        */
        // Handle respawn - only reset on actual death, not on login
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->

        {
            if (newPlayer instanceof PlayerEntityDuck duck) {
                // Only reset if this is a death respawn (not alive)
                if (!alive) {
                    duck.toglide$setIsGliding(false);
                    duck.toglide$setActiveGlider(null);
                    duck.toglide$setIsActivatingGlider(false);
                }
                // Sync the state
                sendSyncPacket(newPlayer, duck);
            }
        });

        // Handle dimension changes - preserve gliding state
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->

        {
            if (oldPlayer instanceof PlayerEntityDuck oldDuck &&
                    newPlayer instanceof PlayerEntityDuck newDuck) {

                // Copy the gliding state for dimension changes
                newDuck.toglide$setIsGliding(oldDuck.toglide$isGliding());
                newDuck.toglide$setIsActivatingGlider(oldDuck.toglide$isActivatingGlider());
                newDuck.toglide$setActiveGlider(oldDuck.toglide$getActiveGlider());

                // Sync after clone
                newPlayer.server.execute(() -> {
                    sendSyncPacket(newPlayer, newDuck);
                });

            }
        });
        // Check glide state every tick - but don't reset on login
        ServerTickEvents.END_SERVER_TICK.register(server ->
        {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PlayerEntityDuck duck = (PlayerEntityDuck) player;
                // Only check ground state if player is actually in the world
                if (player.isAlive() && duck.toglide$isGliding()) {
                    // Stop gliding if on ground or in water
                    if (player.onGround() || player.isInWater() || player.isFallFlying()) {
                        duck.toglide$setIsGliding(false);
                        duck.toglide$setActiveGlider(null);
                        duck.toglide$setIsActivatingGlider(false);
                    }
                    sendSyncPacket(player, duck);
                }
            }
        });
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
        for (ServerPlayer p : PlayerLookup.tracking(sp)) {
            send(packet, p);
        }
        send(packet, sp);
    }

    private static void send(SyncGliderPacket packet, ServerPlayer target) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        SyncGliderPacket.encode(packet, buf);

        ServerPlayNetworking.send(target, ModNetworking.SYNC_GLIDER, buf);
    }

    private static void sendSyncPacketToTarget(ServerPlayer source, PlayerEntityDuck duck, ServerPlayer target) {
        ToGlide.LOG.debug("[DIRECT SYNC] {} → {} | gliding={} activating={}",
                source.getName().getString(),
                target.getName().getString(),
                duck.toglide$isGliding(),
                duck.toglide$isActivatingGlider());

        String gliderId = "";
        if (duck.toglide$getActiveGlider() != null) {
            gliderId = BuiltInRegistries.ITEM
                    .getKey(duck.toglide$getActiveGlider())
                    .toString();
        }

        SyncGliderPacket packet = new SyncGliderPacket(
                source.getId(),
                duck.toglide$isGliding(),
                duck.toglide$isActivatingGlider(),
                gliderId
        );

        send(packet, target);
    }
}