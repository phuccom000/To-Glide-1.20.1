package net.stirdrem.toglide.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.toglide.ToGlide;

public interface ModSounds {
    DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ToGlide.MOD_ID);

    RegistryObject<SoundEvent> GLIDING = register("gliding");
    RegistryObject<SoundEvent> GLIDER_CLOSE = register("glider_close");
    RegistryObject<SoundEvent> GLIDER_OPEN = register("glider_open");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        new ResourceLocation(ToGlide.MOD_ID, name)));
    }

    static void registerSounds(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
        ToGlide.LOG.info("Registering Sounds for " + ToGlide.MOD_ID);
    }
}