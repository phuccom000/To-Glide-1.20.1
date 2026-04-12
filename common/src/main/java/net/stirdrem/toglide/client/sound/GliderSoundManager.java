package net.stirdrem.toglide.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.stirdrem.toglide.PlayerEntityDuck;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class GliderSoundManager {

    private static final GliderSoundManager INSTANCE = new GliderSoundManager();

    // ✅ Track per-player sound
    private final Map<UUID, GlidingWindSoundInstance> activeSounds = new HashMap<>();

    public static GliderSoundManager getInstance() {
        return INSTANCE;
    }

    /**
     * Called every client tick
     */
    public void tick() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) return;

        Iterator<Map.Entry<UUID, GlidingWindSoundInstance>> it = activeSounds.entrySet().iterator();

        while (it.hasNext()) {
            var entry = it.next();
            GlidingWindSoundInstance sound = entry.getValue();

            if (sound.isStopped()) {
                it.remove();
            }
        }

        for (Player player : mc.level.players()) {

            if (!(player instanceof PlayerEntityDuck duck)) continue;

            UUID id = player.getUUID();

            double speedSqr = player.getDeltaMovement().lengthSqr();

            boolean shouldPlay =
                    duck.toglide$isGliding()
                            && !player.onGround()
                            && !player.isInWater()
                            && !player.isFallFlying()
                            && speedSqr > 1.0E-7D;

            if (shouldPlay) {
                if (activeSounds.containsKey(id)) continue;
                GlidingWindSoundInstance existing = activeSounds.get(id);

                if (existing != null && !existing.isStopped()) {
                    continue;
                }

                // start new sound
                GlidingWindSoundInstance sound =
                        new GlidingWindSoundInstance(player, this);

                activeSounds.put(id, sound);

                mc.getSoundManager().play(sound);
            } else {
                // stop if exists
                GlidingWindSoundInstance existing = activeSounds.remove(id);

                if (existing != null) {
                    mc.getSoundManager().stop(existing);
                }
            }
        }
    }

    /**
     * Called by sound instance when it finishes itself
     */
    public void onSoundFinished(GlidingWindSoundInstance sound) {
        activeSounds.values().remove(sound);
        activeSounds.entrySet().removeIf(entry -> entry.getValue() == sound);
    }
}