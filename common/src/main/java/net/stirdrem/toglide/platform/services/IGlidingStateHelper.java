package net.stirdrem.toglide.platform.services;

import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.toglide.networking.SyncGliderPacket;

public interface IGlidingStateHelper {
    void syncToClient(SyncGliderPacket packet, ServerPlayer sp);
}
