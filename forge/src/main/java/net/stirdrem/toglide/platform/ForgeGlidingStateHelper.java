package net.stirdrem.toglide.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;
import net.stirdrem.toglide.platform.services.IGlidingStateHelper;

public class ForgeGlidingStateHelper implements IGlidingStateHelper {
    @Override
    public void syncToClient(SyncGliderPacket packet, ServerPlayer sp) {
        ModNetworking.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> sp),
                packet
        );

        ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> sp),
                packet
        );
    }
}
