package pvpmode.api.client.configuration;

import pvpmode.api.common.configuration.CommonConfiguration;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Process;

/**
 * The configuration interface for the PvP Mode Mod client configuration.
 * 
 * @author CraftedMods
 *
 */
@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "=" + ClientConfiguration.CLIENT_CONFIG_PID)
public interface ClientConfiguration extends CommonConfiguration
{

    public static final String CLIENT_CONFIG_PID = "pvp-mode-client";

    public static final String CLIENT_CATEGORY = "client";

}
