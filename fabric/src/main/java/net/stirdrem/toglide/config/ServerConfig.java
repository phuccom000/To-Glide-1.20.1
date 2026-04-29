package net.stirdrem.toglide.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.toglide.ToGlide;

public class ServerConfig {

    public static final ConfigClassHandler<ServerConfig> HANDLER =
            ConfigClassHandler.createBuilder(ServerConfig.class)
                    .id(new ResourceLocation(ToGlide.MOD_ID, "config"))
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(FabricLoader.getInstance()
                                    .getConfigDir()
                                    .resolve(ToGlide.MOD_ID + ".json5"))
                            .setJson5(true)
                            .build())
                    .build();


    @SerialEntry(comment = "The default color of the glider")
    public String defaultGliderColor = "0xFFFFFF";
    @SerialEntry(comment = "Increase lightning risk while gliding in thunderstorms")
    public boolean enableIncreasedLightningHit = true;
}
