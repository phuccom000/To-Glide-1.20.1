package net.stirdrem.toglide.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.items.GliderItem;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

    public static void handleSyncGlider(SyncGliderPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.level == null) return;

            // 🔥 GET THE TARGET ENTITY (NOT LOCAL PLAYER)
            var entity = minecraft.level.getEntity(packet.getPlayerId());

            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                if (player instanceof PlayerEntityDuck duck) {

                    duck.toglide$setIsGliding(packet.isGliding());
                    duck.toglide$setIsActivatingGlider(packet.isActivating());

                    String gliderId = packet.getGliderId();
                    if (gliderId != null && !gliderId.isEmpty()) {
                        ResourceLocation id = new ResourceLocation(gliderId);

                        if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id)) {
                            var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
                            if (item instanceof GliderItem glider) {
                                duck.toglide$setActiveGlider(glider);
                            }
                        }
                    } else {
                        duck.toglide$setActiveGlider(null);
                    }
                }
            }
        });

        context.setPacketHandled(true);
    }
}