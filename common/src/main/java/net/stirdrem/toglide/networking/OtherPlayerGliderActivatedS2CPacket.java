package net.stirdrem.toglide.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.stirdrem.toglide.PlayerEntityDuck;

public class OtherPlayerGliderActivatedS2CPacket {

    private final int otherPlayerID;
    private final boolean isActivatingGlider;
    private final boolean isPlayerGliding;

    public OtherPlayerGliderActivatedS2CPacket(int id, boolean activating, boolean gliding) {
        this.otherPlayerID = id;
        this.isActivatingGlider = activating;
        this.isPlayerGliding = gliding;
    }

    // ENCODE
    public static void encode(OtherPlayerGliderActivatedS2CPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.otherPlayerID);
        buf.writeBoolean(pkt.isActivatingGlider);
        buf.writeBoolean(pkt.isPlayerGliding);
    }

    // DECODE
    public static OtherPlayerGliderActivatedS2CPacket decode(FriendlyByteBuf buf) {
        return new OtherPlayerGliderActivatedS2CPacket(
                buf.readInt(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    // CLIENT HANDLE
    public static void handleClient(OtherPlayerGliderActivatedS2CPacket pkt) {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            if (mc.level == null) return;

            if (mc.level.getEntity(pkt.otherPlayerID) instanceof AbstractClientPlayer player) {
                if (player instanceof PlayerEntityDuck duck) {

                    duck.toglide$setIsActivatingGlider(pkt.isActivatingGlider);
                    duck.toglide$setIsGliding(pkt.isPlayerGliding);

                    /*// Optional: sound
                    if (pkt.isPlayerGliding) {
                        GliderSoundManager.playSound(new GlidingWindSoundInstance(player));
                    }*/
                }
            }
        });
    }
}