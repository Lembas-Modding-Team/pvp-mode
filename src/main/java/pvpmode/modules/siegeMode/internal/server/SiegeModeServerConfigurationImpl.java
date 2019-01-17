package pvpmode.modules.siegeMode.internal.server;

import java.io.*;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Process;
import pvpmode.internal.common.configuration.AutoForgeConfigurationManager;
import pvpmode.modules.siegeMode.api.server.SiegeModeServerConfiguration;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + SiegeModeServerConfiguration.SIEGE_MODE_SERVER_CONFIG_PID)
public class SiegeModeServerConfigurationImpl extends AutoForgeConfigurationManager
    implements SiegeModeServerConfiguration
{

    public SiegeModeServerConfigurationImpl (Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys, SimpleLogger logger)
    {
        super (configuration, propertyKeys, logger);
    }

    @Override
    protected InputStream openDisplayNameInputStream () throws IOException
    {
        return this.getClass ()
            .getResourceAsStream ("/assets/pvpmode/modules/siegeMode/configurationDisplayNames.properties");
    }

    @Override
    protected InputStream openCommentInputStream () throws IOException
    {
        return this.getClass ()
            .getResourceAsStream ("/assets/pvpmode/modules/siegeMode/configurationComments.properties");
    }

}
