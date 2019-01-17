package pvpmode.internal.common.configuration.keyCreators;

import java.lang.reflect.Method;
import java.util.*;

import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Register;

/**
 * An abstract property key generator for string collections. It uses the
 * {@link Matches} annotation to retrieve a list of valid values.
 * 
 * @author CraftedMods
 * 
 * @param <T>
 *            The collection type
 *
 */
public abstract class StringCollectionPropertyKeyCreator<T extends Collection<String>>
    implements ConfigurationPropertyKeyCreator<T>
{

    @Override
    public ConfigurationPropertyKey<T> create (String processedPropertyName, Class<T> type, String category, Unit unit,
        T defaultValue, Method method)
    {
        Matches matchesAnnotation = method.getAnnotation (Matches.class);

        String[] validValues = null;

        if (matchesAnnotation != null)
        {
            validValues = matchesAnnotation.matches ();
        }

        return create (processedPropertyName, type, category, unit, defaultValue,
            validValues);
    }

    protected abstract ConfigurationPropertyKey<T> create (String processedPropertyName, Class<T> type, String category,
        Unit unit, T defaultValue, String[] validValues);

    /**
     * A configuration property key generator for string list properties.
     * 
     * @author CraftedMods
     *
     */
    @Register(properties = AutoConfigurationConstants.PROPERTY_GENERATOR_TYPE_PROPERTY_KEY + "=java.util.List")
    public static class StringListPropertyKeyCreator extends StringCollectionPropertyKeyCreator<List<String>>
    {

        @Override
        protected ConfigurationPropertyKey<List<String>> create (String processedPropertyName, Class<List<String>> type,
            String category, Unit unit, List<String> defaultValue, String[] validValues)
        {
            return new ConfigurationPropertyKey.StringList (processedPropertyName, category, defaultValue,
                validValues != null ? Arrays.asList (validValues) : null);
        }

    }

    /**
     * A configuration property key generator for string set properties.
     * 
     * @author CraftedMods
     *
     */
    @Register(properties = AutoConfigurationConstants.PROPERTY_GENERATOR_TYPE_PROPERTY_KEY + "=java.util.Set")
    public static class StringSetPropertyKeyCreator extends StringCollectionPropertyKeyCreator<Set<String>>
    {

        @Override
        protected ConfigurationPropertyKey<Set<String>> create (String processedPropertyName, Class<Set<String>> type,
            String category, Unit unit, Set<String> defaultValue, String[] validValues)
        {
            return new ConfigurationPropertyKey.StringSet (processedPropertyName, category, defaultValue,
                validValues != null ? new HashSet<> (Arrays.asList (validValues)) : null);
        }

    }

}
