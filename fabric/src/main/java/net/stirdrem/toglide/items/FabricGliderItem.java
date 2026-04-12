package net.stirdrem.toglide.items;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;
import net.stirdrem.toglide.sounds.ModSounds;

public class FabricGliderItem extends GliderItem {
    public FabricGliderItem(double dropVelocity, double speedFactor, Properties settings) {
        super(dropVelocity, speedFactor, settings);
    }

    @Override
    protected void syncGliderPacket(SyncGliderPacket packet, ServerPlayer sp) {
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

    @Override
    public void playGliderSound(Player player, boolean isOpening) {
        // Server-side: play for all players nearby (including the player)
        player.level().playSound(
                null,  // No specific player, play for all
                player.blockPosition(),
                isOpening ? ModSounds.GLIDER_OPEN : ModSounds.GLIDER_CLOSE,
                SoundSource.PLAYERS,
                1.0F,  // Volume
                1.0F   // Pitch
        );

    }
}
