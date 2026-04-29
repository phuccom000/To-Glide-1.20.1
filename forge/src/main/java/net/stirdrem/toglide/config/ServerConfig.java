package net.stirdrem.toglide.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> defaultGliderColor;
    public static final ForgeConfigSpec.BooleanValue enableIncreasedLightningHit;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        defaultGliderColor = builder
                .comment("The default color of the glider")
                .define("defaultGliderColor", "0xFFFFFF");
        enableIncreasedLightningHit = builder
                .comment("Increase lightning risk while gliding in thunderstorms")
                .define("enableIncreasedLightningHit", true);
        SPEC = builder.build();
    }
}
