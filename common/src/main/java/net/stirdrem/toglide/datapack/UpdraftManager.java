package net.stirdrem.toglide.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.stirdrem.toglide.util.UpdraftData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdraftManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    public static final Map<Block, UpdraftData> BLOCK_DATA = new HashMap<>();
    public static final List<TagEntry> TAG_DATA = new ArrayList<>();

    public UpdraftManager() {
        super(GSON, "updrafts");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap,
                         ResourceManager manager,
                         ProfilerFiller profiler) {

        BLOCK_DATA.clear();
        TAG_DATA.clear();

        for (var entry : jsonMap.entrySet()) {
            JsonObject obj = entry.getValue().getAsJsonObject();

            double strength = obj.get("strength").getAsDouble();
            boolean requiresLit = obj.has("requires_lit") && obj.get("requires_lit").getAsBoolean();

            UpdraftData data = new UpdraftData(strength, requiresLit);

            // --- BLOCK ---
            if (obj.has("block")) {
                ResourceLocation id = new ResourceLocation(obj.get("block").getAsString());
                Block block = BuiltInRegistries.BLOCK.get(id);
                BLOCK_DATA.put(block, data);
            }

            // --- TAG ---
            else if (obj.has("tag")) {
                ResourceLocation id = new ResourceLocation(obj.get("tag").getAsString());
                TagKey<Block> tag = TagKey.create(Registries.BLOCK, id);
                TAG_DATA.add(new TagEntry(tag, data));
            }
        }
    }

    // Helper class
    public static class TagEntry {
        public final TagKey<Block> tag;
        public final UpdraftData data;

        public TagEntry(TagKey<Block> tag, UpdraftData data) {
            this.tag = tag;
            this.data = data;
        }
    }


}
