package pvpmode.internal.common.configuration.keyCreators;

import java.lang.reflect.Method;

import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;
import pvpmode.api.common.configuration.auto.ConfigurationPropertyKeyCreator;
import pvpmode.api.common.utils.Register;

/**
 * A property key creator used for boolean property keys.
 * 
 * @author CraftedMods
 *
 */
@Register
public class BooleanPropertyKeyCreator implements ConfigurationPropertyKeyCreator<Boolean>
{

    @Override
    public ConfigurationPropertyKey<Boolean> create (String processedPropertyName, Class<Boolean> type, String category,
        Unit unit,
        Boolean defaultValue, Method method)
    {
        return defaultValue == null ? new ConfigurationPropertyKey.BooleanKey (processedPropertyName, category)
            : new ConfigurationPropertyKey.BooleanKey (processedPropertyName, category, defaultValue);
    }

}
