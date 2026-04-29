package net.stirdrem.toglide.platform;

import net.stirdrem.toglide.config.ServerConfig;
import net.stirdrem.toglide.platform.services.IConfigHelper;

public class FabricConfigHelper implements IConfigHelper {
    ServerConfig config = ServerConfig.HANDLER.instance();

    @Override
    public String getDefaultGliderColor() {
        return config.defaultGliderColor;
    }

    @Override
    public boolean getEnableIncreasedLightningHit() {
        return config.enableIncreasedLightningHit;
    }
}
