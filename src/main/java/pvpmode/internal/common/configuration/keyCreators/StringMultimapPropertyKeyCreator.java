package pvpmode.internal.common.configuration.keyCreators;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.google.common.collect.Multimap;

import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.*;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Register;

/**
 * An abstract property key generator for string multimaps. It uses the
 * {@link Matches} annotation to retrieve a collection of valid values.
 * 
 * @author CraftedMods
 * 
 */
@Register(properties = AutoConfigurationConstants.PROPERTY_GENERATOR_TYPE_PROPERTY_KEY
    + "=com.google.common.collect.Multimap")
public class StringMultimapPropertyKeyCreator
    implements ConfigurationPropertyKeyCreator<Multimap<String, String>>
{

    @Override
    public ConfigurationPropertyKey<Multimap<String, String>> create (String internalName,
        Class<Multimap<String, String>> type,
        String category, Unit unit, Multimap<String, String> defaultValue, Method method)
    {
        Matches matchesAnnotation = method.getAnnotation (Matches.class);

        Multimap<String, String> validValues = null;

        if (matchesAnnotation != null)
        {
            validValues = StringMultimap
                .fromStringMap (StringMap.fromStringList (Arrays.asList (matchesAnnotation.matches ())));
        }

        return new ConfigurationPropertyKey.StringMultimap (internalName, type, category, defaultValue,
            validValues);
    }

}
