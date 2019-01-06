package pvpmode.modules.lotr.api.server;

import pvpmode.api.common.configuration.ConfigurationManager;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.configuration.ServerConfiguration;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + LOTRServerConfiguration.LOTR_SERVER_CONFIG_PID)
public interface LOTRServerConfiguration extends ConfigurationManager
{

    public static final String LOTR_SERVER_CONFIG_PID = "lotr-compatibility-server";

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default boolean areEnemyBiomeOverridesEnabled ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default boolean isFastTravelingWhilePvPBlocked ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default boolean areSafeBiomeOverridesEnabled ()
    {
        return false;
    }

}
