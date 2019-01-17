package pvpmode.internal.common.configuration;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import cpw.mods.fml.common.Loader;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.*;
import pvpmode.internal.common.utils.ClassDiscoverer;

/**
 * That manager discovers and instantiates the configuration property name
 * mappers. The ASM coremod needs them, so this class will be one of he first
 * that will be instantiated.
 * 
 * @author CraftedMods
 *
 */
public class AutoConfigurationMapperManager
{

    private SimpleLogger logger = PvPCommonUtils.getLogger (AutoConfigurationMapperManager.class);
    private TreeMap<Integer, ConfigurationPropertyNameMapper> mappers = new TreeMap<> ();

    /**
     * This function will be called by the ASM coremod when the relevant classes
     * have been discovered.
     * 
     * @param discoverer
     *            The class discoverer
     * @param timeout
     *            The maximum time it should take to load the classes
     */
    public void processClasspath (ClassDiscoverer discoverer, long timeout)
    {

        Map<Class<? extends Annotation>, Map<String, Set<String>>> foundClasses = discoverer
            .getDiscoveredClassNames (timeout);

        if (foundClasses.containsKey (Register.class))
        {
            if (foundClasses.get (Register.class).containsKey (ConfigurationPropertyNameMapper.class.getName ()))
            {
                mappers = this.getConfigurationPropertyNameMappers (foundClasses.get (Register.class)
                    .get (ConfigurationPropertyNameMapper.class.getName ()));
            }
        }
    }

    private TreeMap<Integer, ConfigurationPropertyNameMapper> getConfigurationPropertyNameMappers (
        Set<String> mapperClassNames)
    {
        TreeMap<Integer, ConfigurationPropertyNameMapper> mappers = new TreeMap<> ();

        PvPCommonUtils
            .createRegisteredInstances (mapperClassNames
                .parallelStream ()
                .map (mapperClassName ->
                {
                    try
                    {
                        return Loader.instance ().getModClassLoader ().loadClass (mapperClassName);
                    }
                    catch (ClassNotFoundException e)
                    {
                        logger.errorThrowable ("Couldn't load the configuration property name mapper class \"%s\"", e,
                            mapperClassName);
                    }
                    return null;
                }).collect (Collectors.toSet ()),
                (clazz, instance) ->
                {
                    Map<String, String> properties = PvPCommonUtils.getPropertiesFromRegisteredClass (clazz);
                    int priority = 30000;
                    if (properties.containsKey (AutoConfigurationConstants.PRIORITY_PROPERTY_KEY))
                    {
                        String priorityString = properties.get (AutoConfigurationConstants.PRIORITY_PROPERTY_KEY);
                        try
                        {
                            priority = Integer
                                .parseInt (priorityString);
                        }
                        catch (NumberFormatException e)
                        {
                            logger.error (
                                "The specified priority \"%s\" for the configuration property name mapper \"%s\" is not a valid number",
                                priorityString, clazz.getName ());
                        }
                    }
                    while (mappers.containsKey (priority))
                    {
                        --priority;
                    }
                    mappers.put (priority, (ConfigurationPropertyNameMapper) instance);
                });
        logger.info ("Instantiated %d configuration property name mappers from the classpath", mappers.size ());
        return mappers;
    }

    /**
     * Returns the internal name of a configuration property, computed with the
     * mappers based on the defined name - the method name.
     * 
     * @param definedName
     *            The method name of the configuration property getter
     * @return The internal name
     */
    public String getInternalName (String definedName)
    {
        String tmpName = definedName;
        for (ConfigurationPropertyNameMapper mapper : this.mappers.descendingMap ().values ())
        {
            tmpName = mapper.toInternalName (tmpName);
        }
        return tmpName;
    }

    /**
     * Returns the computed display name of a configuration property, based on the
     * internal name. This display name doesn't have to be the final one, providers
     * can modify it in any way they want to.
     * 
     * @param internalName
     *            The internal name
     * @return The display name
     */
    public String getDisplayName (String internalName)
    {
        String tmpName = internalName;
        for (ConfigurationPropertyNameMapper mapper : this.mappers.descendingMap ().values ())
        {
            tmpName = mapper.toDisplayName (tmpName);
        }
        return tmpName;
    }

}
