package pvpmode.internal.common.configuration.keyCreators;

import java.lang.reflect.Method;

import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;
import pvpmode.api.common.configuration.auto.ConfigurationPropertyKeyCreator;
import pvpmode.api.common.utils.Register;

/**
 * A property key generator for strings. If the default value is null, the empty
 * string will be used.
 * 
 * @author CraftedMods
 *
 */
@Register
public class StringPropertyKeyCreator implements ConfigurationPropertyKeyCreator<String>
{

    @Override
    public ConfigurationPropertyKey<String> create (String processedPropertyName, Class<String> type, String category,
        Unit unit, String defaultValue, Method method)
    {
        return new ConfigurationPropertyKey.StringKey (processedPropertyName, category,
            defaultValue == null ? "" : defaultValue);
    }

}
