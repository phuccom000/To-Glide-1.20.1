package net.stirdrem.toglide.datapack;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.toglide.ToGlide;

public class FabricUpdraftManager extends UpdraftManager implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.tryBuild(ToGlide.MOD_ID, "updrafts");
    }
}
