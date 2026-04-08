package net.stirdrem.toglide.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ModNetworking {
    public static final ResourceLocation ID = new ResourceLocation("toglide", "sync_glider");

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
            SyncGliderPacket pkt = SyncGliderPacket.decode(buf);
            SyncGliderPacket.handleClient(pkt);
        });
    }
}