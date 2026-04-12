package net.stirdrem.toglide.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.stirdrem.toglide.PlayerEntityDuck;

public class GlidingWindSoundInstance extends AbstractTickableSoundInstance {

    private static final int FADE_IN_START = 20;
    private static final int FADE_IN_END = 40;

    private final Player player;
    private final GliderSoundManager manager;

    private int time;

    public GlidingWindSoundInstance(Player player, GliderSoundManager manager) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());

        this.player = player;
        this.manager = manager;

        this.looping = true;
        this.delay = 0;
        this.volume = 0.1F;
    }

    @Override
    public void tick() {
        ++time;

        if (!(player instanceof PlayerEntityDuck duck)
                || player.isRemoved()
                || !(time <= FADE_IN_START || duck.toglide$isGliding())) {
            stop();
            manager.onSoundFinished(this);
            return;
        }

        this.x = (float) this.player.getX();
        this.y = (float) this.player.getY();
        this.z = (float) this.player.getZ();

        if (this.time < FADE_IN_START) {
            this.volume = 0.0F;
        } else if (this.time < FADE_IN_END) {
            this.volume *= (float) (this.time - FADE_IN_START) / 20.0F;
        }

        float f = (float) this.player.getDeltaMovement().lengthSqr();
        if ((double) f >= 1.0E-7D) {
            this.volume = Mth.clamp(f / 4.0F, 0.0F, 1.0F);
        } else {
            this.volume = 0.0F;
        }

        float f1 = 0.8F;
        if (this.volume > f1) {
            this.pitch = 1.0F + (this.volume - f1);
        } else {
            this.pitch = 1.0F;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GlidingWindSoundInstance other
                && other.player == this.player;
    }
}