package pvpmode.modules.suffixForge.api.server;

import pvpmode.api.common.configuration.ConfigurationManager;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.configuration.ServerConfiguration;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + SuffixForgeServerConfiguration.SUFFIX_FORGE_SERVER_CONFIG_PID)
public interface SuffixForgeServerConfiguration extends ConfigurationManager
{

    public static final String SUFFIX_FORGE_SERVER_CONFIG_PID = "suffix-forge-compatibility-server";

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default boolean areSoulboundItemsDropped ()
    {
        return false;
    }

}
