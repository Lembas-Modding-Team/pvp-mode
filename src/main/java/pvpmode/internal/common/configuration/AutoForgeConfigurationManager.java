package pvpmode.internal.common.configuration;

import java.io.*;
import java.util.*;

import net.minecraftforge.common.config.Configuration;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.*;
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
    protected final SimpleLogger logger;

    protected final Properties configurationDisplayNames = new Properties ();
    protected final Properties configurationComments = new Properties ();

    private String computedPID = null;

    protected AutoForgeConfigurationManager (Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys,
        SimpleLogger logger)
    {
        super (configuration);
        this.logger = logger;
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

    /**
     * Returns an input stream leading to the display name property source.
     * 
     * @return The input stream for the properties
     * @throws IOException
     *             If IO errors occur
     */
    protected InputStream openDisplayNameInputStream () throws IOException
    {
        return this.getClass ().getClassLoader ()
            .getResourceAsStream (this.getClass ().getPackage ().getName ().replaceAll ("\\.", "/")
                + "/configurationDisplayNames.properties");
    }

    /**
     * Returns an input stream leading to the comment property source.
     * 
     * @return The input stream for the properties
     * @throws IOException
     *             If IO errors occur
     */
    protected InputStream openCommentInputStream () throws IOException
    {
        return this.getClass ().getClassLoader ()
            .getResourceAsStream (this.getClass ().getPackage ().getName ().replaceAll ("\\.", "/")
                + "/configurationComments.properties");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<ConfigurationPropertyKey<?>, Object> retrieveProperties ()
    {
        Map<ConfigurationPropertyKey<?>, Object> ret = new HashMap<> ();
        for (ConfigurationPropertyKey<?> key : propertyKeys.values ())
        {
            if (Boolean.class.isAssignableFrom (key.getValueType ()))
            {
                ret.put (key,
                    getBoolean ((ConfigurationPropertyKey<Boolean>) key, (Boolean) key.getDefaultValue (),
                        getComment (key)));
            }
            else if (Enum.class.isAssignableFrom (key.getValueType ()))
            {
                ret.put (key,
                    this.getEnum ((ConfigurationPropertyKey<? extends Enum<?>>) key,
                        (Enum<?>) key.getDefaultValue ()));
            }
            else if (key instanceof IntegerKey)
            {
                IntegerKey intKey = (IntegerKey) key;
                ret.put (intKey,
                    getInt (intKey, intKey.getDefaultValue (), intKey.getMinValue (), intKey.getMaxValue (),
                        getComment (key)));
            }
            else if (key instanceof FloatKey)
            {
                FloatKey floatKey = (FloatKey) key;
                ret.put (floatKey,
                    getFloat (floatKey, floatKey.getDefaultValue (), floatKey.getMinValue (),
                        floatKey.getMaxValue (),
                        getComment (key)));
            }
            else if (String.class.isAssignableFrom (key.getValueType ()))
            {
                ret.put (key, getString ((ConfigurationPropertyKey<String>) key, (String) key.getDefaultValue (),
                    getComment (key)));
            }
            else if (key instanceof StringList)
            {
                ret.put (key,
                    getStringList ((ConfigurationPropertyKey<List<String>>) key,
                        (List<String>) key.getDefaultValue (),
                        (List<String>) ((StringList) key).getValidValues (), getComment (key)));
            }
            else if (key instanceof StringSet)
            {
                ret.put (key,
                    getStringSet ((ConfigurationPropertyKey<Set<String>>) key,
                        (Set<String>) key.getDefaultValue (),
                        (Set<String>) ((StringSet) key).getValidValues (), getComment (key)));
            }
            else
            {
                logger.warning ("The type \"%s\" of the configuration property key \"%s\" is not supported",
                    key.getValueType (),
                    key.getInternalName ());
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<?>, J extends Enum<J>> Enum<?> getEnum (ConfigurationPropertyKey<T> key,
        Enum<?> defaultValue)
    {
        return this.getEnum ((ConfigurationPropertyKey.EnumKey<J>) (ConfigurationPropertyKey<?>) key, (J) defaultValue,
            getComment (key));
    }

    private String getProperty (ConfigurationPropertyKey<?> propertyKey, Properties properties, String defaultValue)
    {
        String prop = properties.getProperty (propertyKey.getInternalName ());
        return prop == null ? defaultValue : prop;
    }

    /**
     * Returns the configuration comment for the specified property key or an empty
     * string of none was found.
     * 
     * @param propertyKey
     *            The configuration property key
     * @return The comment for that key
     */
    public String getComment (ConfigurationPropertyKey<?> propertyKey)
    {
        return getProperty (propertyKey, configurationComments, "");
    }

    /**
     * Returns the display name for the specified property key. The manager will
     * first attempt to look at the specified property source
     * {@link AutoForgeConfigurationManager#openDisplayNameInputStream()} for the
     * display name, if that fails, it'll use the
     * {@link AutoConfigurationMapperManager}.
     * 
     * @param propertyKey
     *            The configuration property key
     * @return The display name for that key
     */
    public String getDisplayName (ConfigurationPropertyKey<?> propertyKey)
    {
        String displayName = getProperty (propertyKey, configurationDisplayNames, propertyKey.getInternalName ());
        if (displayName.equals (propertyKey.getInternalName ()) && propertyKeys.containsValue (propertyKey))
        {
            displayName = PvPMode.proxy.getAutoConfigMapperManager ().getDisplayName (propertyKey.getInternalName ());
        }
        return displayName;
    }

    @Override
    protected String getNameWithUnit (ConfigurationPropertyKey<?> key)
    {
        return this.getNameWithUnit (getDisplayName (key), key.getUnit ());
    }

}
