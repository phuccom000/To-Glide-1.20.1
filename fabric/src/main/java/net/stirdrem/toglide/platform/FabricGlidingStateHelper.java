package net.stirdrem.toglide.platform;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;
import net.stirdrem.toglide.platform.services.IGlidingStateHelper;

public class FabricGlidingStateHelper implements IGlidingStateHelper {
    @Override
    public void syncToClient(SyncGliderPacket packet, ServerPlayer sp) {
        for (ServerPlayer p : PlayerLookup.tracking(sp)) {
            send(packet, p);
        }
        send(packet, sp);
    }

    private void send(SyncGliderPacket packet, ServerPlayer target) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        SyncGliderPacket.encode(packet, buf);

        ServerPlayNetworking.send(target, ModNetworking.SYNC_GLIDER, buf);
    }
}
