package net.stirdrem.toglide.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.ToGlide;
import net.stirdrem.toglide.items.GliderItem;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;

public class GliderEventHandler {
    public static void register() {
        // Sync when player joins - AFTER they're fully loaded
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer joiningPlayer = handler.getPlayer();

            server.execute(() -> {
                ToGlide.LOG.info("=== PLAYER JOIN: {} ===", joiningPlayer.getName().getString());

                // LOG ALL PLAYERS STATE
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    PlayerEntityDuck d = (PlayerEntityDuck) p;

                    ToGlide.LOG.info("[STATE] Player: {} | gliding={} | activating={} | glider={}",
                            p.getName().getString(),
                            d.toglide$isGliding(),
                            d.toglide$isActivatingGlider(),
                            d.toglide$getActiveGlider() != null
                                    ? BuiltInRegistries.ITEM.getKey(d.toglide$getActiveGlider())
                                    : "null"
                    );
                }

                // 1. Sync ALL OTHER players → joining player
                for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                    if (other == joiningPlayer) continue;

                    if (other instanceof PlayerEntityDuck duck) {
                        ToGlide.LOG.info("[SEND] {} → {} | gliding={}",
                                other.getName().getString(),
                                joiningPlayer.getName().getString(),
                                duck.toglide$isGliding());

                        sendSyncPacketToTarget(other, duck, joiningPlayer);
                    }
                }

                // 2. Sync joining player → everyone else
                if (joiningPlayer instanceof PlayerEntityDuck duck) {
                    validateAndFixGliderState(joiningPlayer, duck);

                    ToGlide.LOG.info("[SEND] {} → ALL | gliding={}",
                            joiningPlayer.getName().getString(),
                            duck.toglide$isGliding());

                    sendSyncPacket(joiningPlayer, duck);
                }
            });
        });

        // Handle respawn - only reset on actual death, not on login
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
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
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
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
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PlayerEntityDuck duck = (PlayerEntityDuck) player;

                // Only check ground state if player is actually in the world
                if (player.isAlive() && duck.toglide$isGliding()) {
                    // Stop gliding if on ground or in water
                    if (player.onGround() || player.isInWater() || player.isFallFlying()) {
                        duck.toglide$setIsGliding(false);
                        duck.toglide$setActiveGlider(null);
                        duck.toglide$setIsActivatingGlider(false);
                        sendSyncPacket(player, duck);
                    }
                }
            }
        });
    }

    private static void validateAndFixGliderState(ServerPlayer player, PlayerEntityDuck duck) {
        // Only fix if there's an inconsistency
        if (duck.toglide$isGliding() && duck.toglide$getActiveGlider() != null) {
            GliderItem activeGlider = duck.toglide$getActiveGlider();
            boolean hasGlider = (player.getMainHandItem().getItem() == activeGlider) ||
                    (player.getOffhandItem().getItem() == activeGlider);

            if (!hasGlider) {
                // Glider no longer in inventory - reset state
                duck.toglide$setIsGliding(false);
                duck.toglide$setActiveGlider(null);
                duck.toglide$setIsActivatingGlider(false);
            }
        }

        // Don't resume gliding if on ground (but preserve if in air)
        if (player.onGround() && duck.toglide$isGliding()) {
            duck.toglide$setIsGliding(false);
            duck.toglide$setActiveGlider(null);
            duck.toglide$setIsActivatingGlider(false);
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
        ToGlide.LOG.info("[SYNC] Sending packet for {} | gliding={} activating={}",
                sp.getName().getString(),
                packet.isGliding(),
                packet.isActivating());

        FriendlyByteBuf buf = PacketByteBufs.create();
        SyncGliderPacket.encode(packet, buf);

        // self
        ServerPlayNetworking.send(sp, ModNetworking.SYNC_GLIDER, buf);

        // others
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(sp)) {
            if (trackingPlayer != sp) {
                ToGlide.LOG.info("[SYNC] {} → {}",
                        sp.getName().getString(),
                        trackingPlayer.getName().getString());

                FriendlyByteBuf trackingBuf = PacketByteBufs.create();
                SyncGliderPacket.encode(packet, trackingBuf);
                ServerPlayNetworking.send(trackingPlayer, ModNetworking.SYNC_GLIDER, trackingBuf);
            }
        }
    }

    private static void sendSyncPacketToTarget(ServerPlayer source, PlayerEntityDuck duck, ServerPlayer target) {
        ToGlide.LOG.info("[DIRECT SYNC] {} → {} | gliding={} activating={}",
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

        FriendlyByteBuf buf = PacketByteBufs.create();
        SyncGliderPacket.encode(packet, buf);

        ServerPlayNetworking.send(target, ModNetworking.SYNC_GLIDER, buf);
        syncGliderPacket(packet, target);
    }
}