package pvpmode.modules.lotr.internal.server;

import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Process;
import pvpmode.internal.common.configuration.AutoForgeConfigurationManager;
import pvpmode.modules.lotr.api.server.LOTRServerConfiguration;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + LOTRServerConfiguration.LOTR_SERVER_CONFIG_PID)
public class LOTRServerConfigurationImpl extends AutoForgeConfigurationManager implements LOTRServerConfiguration
{

    private final LOTRModCompatibilityModule module;

    protected LOTRServerConfigurationImpl (Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys, SimpleLogger logger, LOTRModCompatibilityModule module)
    {
        super (configuration, propertyKeys, logger);
        this.module = module;
    }

    @Override
    public void load ()
    {
        super.load ();
        module.reloadConfiguration (this);
    }

}
