package pvpmode.internal.common.configuration.keyCreators;

import java.lang.reflect.Method;
import java.util.*;

import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.*;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Register;

/**
 * An abstract property key generator for string maps. It uses the
 * {@link Matches} annotation to retrieve a collection of valid values.
 * 
 * @author CraftedMods
 * 
 */
@Register(properties = AutoConfigurationConstants.PROPERTY_GENERATOR_TYPE_PROPERTY_KEY + "=java.util.Map")
public class StringMapPropertyKeyCreator
    implements ConfigurationPropertyKeyCreator<Map<String, String>>
{

    @Override
    public ConfigurationPropertyKey<Map<String, String>> create (String internalName, Class<Map<String, String>> type,
        String category, Unit unit, Map<String, String> defaultValue, Method method)
    {
        Matches matchesAnnotation = method.getAnnotation (Matches.class);

        Map<String, String> validValues = null;

        if (matchesAnnotation != null)
        {
            validValues = StringMap.fromStringList (Arrays.asList (matchesAnnotation.matches ()));
        }

        return new ConfigurationPropertyKey.StringMap (internalName, type, category, defaultValue,
            validValues);
    }

}
