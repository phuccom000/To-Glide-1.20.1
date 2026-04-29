package net.stirdrem.toglide.platform;

import net.stirdrem.toglide.config.ServerConfig;
import net.stirdrem.toglide.platform.services.IConfigHelper;

public class ForgeConfigHelper implements IConfigHelper {

    @Override

    public String getDefaultGliderColor() {
        return ServerConfig.defaultGliderColor.get();
    }

    @Override
    public boolean getEnableIncreasedLightningHit() {
        return ServerConfig.enableIncreasedLightningHit.get();
    }
}
