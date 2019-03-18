package pvpmode.modules.enderio.api.server;

import pvpmode.api.common.configuration.ConfigurationManager;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.configuration.ServerConfiguration;

@Process(properties =
{AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + EnderIOServerConfiguration.ENDER_IO_SERVER_CONFIG_PID,
    AutoConfigurationConstants.MANUAL_PROCESSING_PROPERTY_KEY + "=true"})
public interface EnderIOServerConfiguration extends ConfigurationManager
{

    public static final String ENDER_IO_SERVER_CONFIG_PID = "ender-io-compatibility-server";

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default boolean areSoulboundItemsDropped ()
    {
        return false;
    }

}
