package pvpmode.internal.common.configuration;

import java.io.*;
import java.util.*;

import net.minecraftforge.common.config.Configuration;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.*;
import pvpmode.api.common.utils.Process;

/**
 * A configuration manager using the MinecraftForge configuration system and the
 * AutoConfiguration environment.
 * 
 * @author CraftedMods
 *
 */
public abstract class AutoForgeConfigurationManager extends ForgeConfigurationManager
    implements AutoConfigurationManager
{

    private Map<String, ConfigurationPropertyKey<?>> propertyKeys;

    protected final Properties configurationDisplayNames = new Properties ();
    protected final Properties configurationComments = new Properties ();

    private String computedPID = null;

    protected AutoForgeConfigurationManager (Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys,
        SimpleLogger logger)
    {
        super (configuration, logger);
        this.propertyKeys = propertyKeys;

        this.loadProperties (this::openDisplayNameInputStream, configurationDisplayNames, "display name");
        this.loadProperties (this::openCommentInputStream, configurationComments, "comment");
    }

    @Override
    public String getPID ()
    {
        if (computedPID == null)
        {
            Process processAnnotation = this.getClass ().getAnnotation (Process.class);
            if (processAnnotation != null)
            {
                Map<String, String> properties = PvPCommonUtils.getPropertiesFromProcessedClass (this.getClass ());
                if (properties.containsKey (AutoConfigurationConstants.PID_PROPERTY_KEY))
                    computedPID = properties
                        .get (AutoConfigurationConstants.PID_PROPERTY_KEY);
            }
            if (computedPID == null)
                computedPID = AutoConfigurationConstants.DEFAULT_PID;
        }
        return computedPID;
    }

    private void loadProperties (FailableSupplier<InputStream, IOException> supplier, Properties properties,
        String type)
    {
        try (InputStream in = supplier.get ())
        {
            if (in != null)
            {
                logger.debug (
                    "A configuration %s property file was found for the configuration manager with the PID \"%s\"",
                    type,
                    getPID ());
                properties.load (in);
                logger.debug ("Loaded %d %ss for the configuration manager with the PID \"%s\"", properties.size (),
                    type,
                    getPID ());
            }
        }
        catch (IOException e)
        {
            logger.errorThrowable (
                "Couldn't retrieve the configuration %ss from the specified sources", e, type);
        }
    }

    @Override
    protected Map<String, ConfigurationPropertyKey<?>> getRegisteredPropertyKeys ()
    {
        return this.propertyKeys;
    }

    /**
     * Returns an input stream leading to the display name property source.
     * 
     * @return The input stream for the properties
     * @throws IOException
     *             If IO errors occur
     */
    protected abstract InputStream openDisplayNameInputStream () throws IOException;

    /**
     * Returns an input stream leading to the comment property source.
     * 
     * @return The input stream for the properties
     * @throws IOException
     *             If IO errors occur
     */
    protected abstract InputStream openCommentInputStream () throws IOException;

    private String getProperty (ConfigurationPropertyKey<?> propertyKey, Properties properties, String defaultValue)
    {
        String prop = properties.getProperty (propertyKey.getInternalName ());
        return prop == null ? defaultValue : prop;
    }

    @Override
    public String getComment (ConfigurationPropertyKey<?> key)
    {
        return getProperty (key, configurationComments, "");
    }

    @Override
    public String getDisplayName (ConfigurationPropertyKey<?> key)
    {
        String displayName = getProperty (key, configurationDisplayNames, key.getInternalName ());
        if (displayName.equals (key.getInternalName ()) && propertyKeys.containsValue (key))
        {
            displayName = PvPMode.proxy.getAutoConfigMapperManager ().getDisplayName (key.getInternalName ());
        }
        return displayName;
    }

}
