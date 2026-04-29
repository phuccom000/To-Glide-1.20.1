package net.stirdrem.toglide.event;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.toglide.datapack.UpdraftManager;

@Mod.EventBusSubscriber(modid = "toglide")
public class ModReloadListeners {

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new UpdraftManager());
    }
}