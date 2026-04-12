package net.stirdrem.toglide;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.stirdrem.toglide.client.sound.GliderSoundManager;
import net.stirdrem.toglide.items.GliderItem;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;

public class GlidingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModNetworking.registerClient();

        ClientPlayNetworking.registerGlobalReceiver(
                ModNetworking.SYNC_GLIDER,
                (client, handler, buf, responseSender) -> {
                    SyncGliderPacket packet = SyncGliderPacket.decode(buf);

                    client.execute(() -> {
                        if (client.level == null) return;

                        // IMPORTANT: get correct entity, NOT client.player
                        var entity = client.level.getEntity(packet.getPlayerId());

                        if (entity instanceof net.minecraft.world.entity.player.Player player) {
                            if (player instanceof PlayerEntityDuck duck) {

                                duck.toglide$setIsGliding(packet.isGliding());
                                duck.toglide$setIsActivatingGlider(packet.isActivating());

                                if (packet.getGliderId() != null && !packet.getGliderId().isEmpty()) {
                                    var id = new net.minecraft.resources.ResourceLocation(packet.getGliderId());

                                    if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id)) {
                                        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
                                        if (item instanceof GliderItem glider) {
                                            duck.toglide$setActiveGlider(glider);
                                        }
                                    }
                                } else {
                                    duck.toglide$setActiveGlider(null);
                                }

                                ToGlide.LOG.debug("[CLIENT] Applied glider for {} | gliding={}", player.getName().getString(), packet.isGliding());
                            }
                        } else {
                            ToGlide.LOG.debug("[CLIENT] Entity not found for id={}", packet.getPlayerId());
                        }
                    });
                }
        );


        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof GliderItem)
                ItemProperties.register(item,
                        new net.minecraft.resources.ResourceLocation("gliding"),
                        (stack, world, entity, seed) -> {
                            if (entity instanceof PlayerEntityDuck duck && duck.toglide$isGliding()) {
                                return 1.0F;
                            }
                            return 0.0F;
                        });
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.isPaused() || client.player == null) return;
            GliderSoundManager.getInstance().tick();
        });
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof GliderItem glider)
                ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return glider.getColor(stack);
                    }
                    return 0xFFFFFF;
                }, item);
        }
    }
}
