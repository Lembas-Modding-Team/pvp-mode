package pvpmode.internal.common.configuration;

import java.io.*;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.configuration.*;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Process;
import pvpmode.internal.common.CommonProxy;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "=" + CommonConfiguration.COMMON_CONFIG_PID)
public class CommonConfigurationImpl extends AutoForgeConfigurationManager implements CommonConfiguration
{

    public CommonConfigurationImpl (CommonProxy proxy, Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys)
    {
        super (configuration, propertyKeys,
            proxy.getLogger ());
    }

    @Override
    protected InputStream openDisplayNameInputStream () throws IOException
    {
        return this.getClass ().getResourceAsStream ("/assets/pvpmode/configurationDisplayNames.properties");
    }

    @Override
    protected InputStream openCommentInputStream () throws IOException
    {
        return this.getClass ().getResourceAsStream ("/assets/pvpmode/configurationComments.properties");
    }

}
