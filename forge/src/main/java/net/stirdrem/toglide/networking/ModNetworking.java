package net.stirdrem.toglide.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("toglide", "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;

        CHANNEL.registerMessage(
                id++,
                SyncGliderPacket.class,
                SyncGliderPacket::encode,
                SyncGliderPacket::decode,
                (pkt, ctx) -> {
                    ctx.get().enqueueWork(() -> SyncGliderPacket.handleClient(pkt));
                    ctx.get().setPacketHandled(true);
                }
        );
        CHANNEL.registerMessage(
                id++,
                OtherPlayerGliderActivatedS2CPacket.class,
                OtherPlayerGliderActivatedS2CPacket::encode,
                OtherPlayerGliderActivatedS2CPacket::decode,
                (pkt, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        OtherPlayerGliderActivatedS2CPacket.handleClient(pkt);
                    });
                    ctx.get().setPacketHandled(true);
                }
        );
    }
}