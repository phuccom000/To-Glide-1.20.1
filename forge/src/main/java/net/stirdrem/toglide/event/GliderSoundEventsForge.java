package net.stirdrem.toglide.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.toglide.PlayerEntityDuck;
import net.stirdrem.toglide.client.sound.GliderSoundManager;


@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class GliderSoundEventsForge {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (mc.player == null) return;
        if (mc.isPaused()) return;
        if (mc.player instanceof PlayerEntityDuck duck && !duck.toglide$isGliding()) return;
        GliderSoundManager.getInstance().tick();
    }
}