package net.stirdrem.toglide.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import net.stirdrem.toglide.networking.ModNetworking;
import net.stirdrem.toglide.networking.SyncGliderPacket;
import net.stirdrem.toglide.sounds.ModSounds;

public class ForgeGliderItem extends GliderItem {

    public ForgeGliderItem(double dropVelocity, double speedFactor, Properties settings) {
        super(dropVelocity, speedFactor, settings);
    }

    @Override
    protected void syncGliderPacket(SyncGliderPacket packet, ServerPlayer sp) {
        ModNetworking.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> sp),
                packet
        );

        ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> sp),
                packet
        );
    }

    @Override
    public void playGliderSound(Player player, boolean isOpening) {

        // Server-side: play for all players nearby (including the player)
        player.level().playSound(
                null,  // No specific player, play for all
                player.blockPosition(),
                isOpening ? ModSounds.GLIDER_OPEN.get() : ModSounds.GLIDER_CLOSE.get(),
                SoundSource.PLAYERS,
                1.0F,  // Volume
                1.0F   // Pitch
        );
    }
}