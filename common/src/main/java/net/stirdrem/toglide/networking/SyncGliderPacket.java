package net.stirdrem.toglide.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.items.GliderItem;

public class SyncGliderPacket {
    public boolean isGliding;
    public boolean isActivating;
    public String gliderId; // registry id

    public SyncGliderPacket(boolean g, boolean a, String id) {
        this.isGliding = g;
        this.isActivating = a;
        this.gliderId = id;
    }

    // ENCODE
    public static void encode(SyncGliderPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.isGliding);
        buf.writeBoolean(pkt.isActivating);
        buf.writeUtf(pkt.gliderId == null ? "" : pkt.gliderId);
    }

    // DECODE
    public static SyncGliderPacket decode(FriendlyByteBuf buf) {
        return new SyncGliderPacket(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readUtf()
        );
    }

    // HANDLE (CLIENT)
    public static void handleClient(SyncGliderPacket pkt) {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            if (mc.player == null) return;

            PlayerEntityDuck duck = (PlayerEntityDuck) mc.player;

            duck.toglide$setIsGliding(pkt.isGliding);
            duck.toglide$setIsActivatingGlider(pkt.isActivating);

            if (!pkt.gliderId.isEmpty()) {
                ResourceLocation id = new ResourceLocation(pkt.gliderId);
                if (BuiltInRegistries.ITEM.get(id) instanceof GliderItem glider) {
                    duck.toglide$setActiveGlider(glider);
                }
            }
        });
    }
}