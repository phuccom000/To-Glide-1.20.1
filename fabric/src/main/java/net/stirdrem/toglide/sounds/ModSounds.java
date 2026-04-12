package net.stirdrem.toglide.sounds;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.stirdrem.toglide.ToGlide;

public interface ModSounds {
    SoundEvent GLIDING = registerSound("gliding");
    SoundEvent GLIDER_OPEN = registerSound("glider_open");
    SoundEvent GLIDER_CLOSE = registerSound("glider_close");

    private static SoundEvent registerSound(String name) {
        ResourceLocation id = new ResourceLocation(ToGlide.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    static void registerSounds() {
        ToGlide.LOG.info("Registering Sounds for " + ToGlide.MOD_ID);
    }
}