package net.stirdrem.toglide.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.stirdrem.toglide.ToGlide;

public class ModNetworking {
    private static final String PROTOCOL = "1";
    private static int id = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ToGlide.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        // Register SyncGliderPacket (for self)
        CHANNEL.registerMessage(
                id++,
                SyncGliderPacket.class,
                SyncGliderPacket::encode,
                SyncGliderPacket::decode,
                (packet, context) -> {
                    context.get().enqueueWork(() -> {
                        // Handle on client side
                        if (context.get().getDirection().getReceptionSide().isClient()) {
                            ClientPacketHandler.handleSyncGlider(packet, context);
                        }
                    });
                    context.get().setPacketHandled(true);
                }
        );

        // Register OtherPlayerGliderActivatedS2CPacket
        CHANNEL.registerMessage(
                id++,
                OtherPlayerGliderActivatedS2CPacket.class,
                OtherPlayerGliderActivatedS2CPacket::encode,
                OtherPlayerGliderActivatedS2CPacket::decode,
                (packet, context) -> {
                    context.get().enqueueWork(() -> {
                        if (context.get().getDirection().getReceptionSide().isClient()) {
                            OtherPlayerGliderActivatedS2CPacket.handleClient(packet);
                        }
                    });
                    context.get().setPacketHandled(true);
                }
        );
    }
}