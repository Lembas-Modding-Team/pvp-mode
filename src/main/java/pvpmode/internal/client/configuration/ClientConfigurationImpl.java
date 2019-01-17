package pvpmode.internal.client.configuration;

import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import pvpmode.api.client.configuration.ClientConfiguration;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Process;
import pvpmode.internal.common.CommonProxy;
import pvpmode.internal.common.configuration.CommonConfigurationImpl;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "=" + ClientConfiguration.CLIENT_CONFIG_PID)
public class ClientConfigurationImpl extends CommonConfigurationImpl implements ClientConfiguration
{

    public ClientConfigurationImpl (CommonProxy proxy, Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys)
    {
        super (proxy, configuration, propertyKeys);
    }

}
