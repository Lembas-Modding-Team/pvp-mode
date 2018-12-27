package pvpmode.internal.common.configuration.keyCreators;

import java.lang.reflect.Method;

import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;
import pvpmode.api.common.configuration.auto.ConfigurationPropertyKeyCreator;
import pvpmode.api.common.utils.Register;

/**
 * A generic property key creator for enum properties.
 * 
 * @author CraftedMods
 *
 */
@Register
public class EnumPvPModePropertyKeyGenerator implements ConfigurationPropertyKeyCreator<Enum<?>>
{

    @Override
    @SuppressWarnings(
    {"rawtypes", "unchecked"})
    public ConfigurationPropertyKey<Enum<?>> create (String processedPropertyName, Class<Enum<?>> type,
        String category, Unit unit, Enum<?> defaultValue, Method method)
    {
        return new ConfigurationPropertyKey.EnumKey (processedPropertyName, type, category, unit,
            defaultValue);
    }

}
