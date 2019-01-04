package pvpmode.api.common.configuration;

import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Process;

/**
 * An interface containing the common configuration API of the PvP Mode Mod.
 * 
 * @author CraftedMods
 *
 */
@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "=" + CommonConfiguration.COMMON_CONFIG_PID)
public interface CommonConfiguration extends AutoConfigurationManager
{

    public static final String COMMON_CONFIG_PID = "pvp-mode-common";

    public static final String COMMON_CONFIGURATION_CATEGORY = "common";

    @ConfigurationPropertyGetter(category = COMMON_CONFIGURATION_CATEGORY)
    public default boolean isVersionCheckerEnabled ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = COMMON_CONFIGURATION_CATEGORY)
    public default boolean isNewVersionAnnouncedInChat ()
    {
        return true;
    }

}