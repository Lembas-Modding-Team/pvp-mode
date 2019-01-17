package pvpmode.api.common.configuration.auto;

import java.lang.reflect.Method;

import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;
import pvpmode.api.common.utils.Register;

/**
 * A Configuration Property Key Creator creates a configuration property key
 * based on data extracted from the configuration interface. It can also use
 * additional, provider specific informations like method annotations and so on
 * to create a configuration property key. These key creators can be registered
 * with the {@link Register} annotation. The {@link Register#properties()} array
 * can contain a property with the key
 * {@link AutoConfigurationConstants#PROPERTY_GENERATOR_TYPE_PROPERTY_KEY} which
 * specifies the type of the generated property key. It could be necessary if
 * the implementing class doesn't implement the interface directly, so the
 * providers can get the provided type easily. If it does, it's guaranteed that
 * this property isn't needed.
 * 
 * @author CraftedMods
 *
 * @param <T>
 *            The type the configuration property key to be generated holds
 */
public interface ConfigurationPropertyKeyCreator<T>
{
    /**
     * Creates a configuration property key from the supplied data.
     * 
     * @param internalName
     *            The internal name of the configuration property
     * @param type
     *            The type the property will hold
     * @param category
     *            The category the property is assigned to
     * @param unit
     *            The unit the property has
     * @param defaultValue
     *            The default value of the property or null, if no value was
     *            specified
     * @param method
     *            The method of the configuration interface the property was
     *            specified with
     * @return The configuration property key
     */
    public ConfigurationPropertyKey<T> create (String internalName, Class<T> type, String category, Unit unit,
        T defaultValue, Method method);

}
