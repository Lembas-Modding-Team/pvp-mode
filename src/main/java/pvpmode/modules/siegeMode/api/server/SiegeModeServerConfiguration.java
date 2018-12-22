package pvpmode.modules.siegeMode.api.server;

import pvpmode.api.common.configuration.ConfigurationManager;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.configuration.ServerConfiguration;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + SiegeModeServerConfiguration.SIEGE_MODE_SERVER_CONFIG_PID)
public interface SiegeModeServerConfiguration extends ConfigurationManager
{

    public static final String SIEGE_MODE_SERVER_CONFIG_PID = "siege-mode-compatibility-server";

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default boolean isPvPLoggingDuringSiegesDisabled ()
    {
        return true;
    }

}
