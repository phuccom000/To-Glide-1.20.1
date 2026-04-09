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
        FriendlyByteBuf buf =
                PacketByteBufs.create();
        SyncGliderPacket.encode(packet, buf);

        // send to others
        for (ServerPlayer p :
                PlayerLookup.tracking(sp)) {

            ServerPlayNetworking.send(
                    p,
                    ModNetworking.SYNC_GLIDER,
                    buf
            );
        }

        ServerPlayNetworking.send(
                sp,
                ModNetworking.SYNC_GLIDER,
                buf
        );
    }
}
