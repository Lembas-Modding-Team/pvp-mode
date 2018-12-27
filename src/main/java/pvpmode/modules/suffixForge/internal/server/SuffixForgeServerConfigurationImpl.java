package pvpmode.modules.suffixForge.internal.server;

import java.io.*;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Process;
import pvpmode.internal.common.configuration.AutoForgeConfigurationManager;
import pvpmode.modules.suffixForge.api.server.SuffixForgeServerConfiguration;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + SuffixForgeServerConfiguration.SUFFIX_FORGE_SERVER_CONFIG_PID)
public class SuffixForgeServerConfigurationImpl extends AutoForgeConfigurationManager
    implements SuffixForgeServerConfiguration
{

    public SuffixForgeServerConfigurationImpl (Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys, SimpleLogger logger)
    {
        super (configuration, propertyKeys, logger);
    }

    @Override
    protected InputStream openDisplayNameInputStream () throws IOException
    {
        return this.getClass ()
            .getResourceAsStream ("/assets/pvpmode/modules/suffixForge/configurationDisplayNames.properties");
    }

    @Override
    protected InputStream openCommentInputStream () throws IOException
    {
        return this.getClass ()
            .getResourceAsStream ("/assets/pvpmode/modules/suffixForge/configurationComments.properties");
    }

}
