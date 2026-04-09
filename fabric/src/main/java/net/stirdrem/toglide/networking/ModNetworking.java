package net.stirdrem.toglide.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ModNetworking {
    public static final ResourceLocation SYNC_GLIDER = new ResourceLocation("toglide", "sync_glider");
    public static final ResourceLocation OTHER_GLIDING = new ResourceLocation("toglide", "otherplayerglidings2c");

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                SYNC_GLIDER,
                (client, handler, buf, responseSender) -> {
                    SyncGliderPacket pkt = SyncGliderPacket.decode(buf);
                    SyncGliderPacket.handleClient(pkt);
                });
        ClientPlayNetworking.registerGlobalReceiver(
                OTHER_GLIDING,
                (client, handler, buf, responseSender) -> {
                    OtherPlayerGliderActivatedS2CPacket pkt =
                            OtherPlayerGliderActivatedS2CPacket.decode(buf);
                    OtherPlayerGliderActivatedS2CPacket.handleClient(pkt);
                }
        );
    }
}