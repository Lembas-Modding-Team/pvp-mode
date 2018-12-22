package pvpmode.internal.common.configuration.keyCreators;

import java.lang.reflect.Method;

import akka.io.Tcp.Bound;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Register;

/**
 * A generic property key creator base class for number properties. It used the
 * {@link Bound} annotation to retrieve the minimum and maximum valid values.
 * 
 * @author CraftedMods
 *
 * @param <T>
 *            The actual number type
 */
public abstract class NumberPropertyKeyCreator<T extends Number> implements ConfigurationPropertyKeyCreator<T>
{

    @Override
    public ConfigurationPropertyKey<T> create (String processedPropertyName, Class<T> type, String category,
        Unit unit, T defaultValue, Method method)
    {
        Bounded boundsAnnotation = method.getAnnotation (Bounded.class);

        T minValue = getDefaultMinValue ();
        T maxValue = getDefaultMaxValue ();

        if (boundsAnnotation != null)
        {
            if (!boundsAnnotation.min ().equals (""))
            {
                minValue = parseNumber (boundsAnnotation.min ());
            }
            if (!boundsAnnotation.max ().equals (""))
            {
                maxValue = parseNumber (boundsAnnotation.max ());
            }
        }

        return createPropertyKey (processedPropertyName, category, unit, defaultValue, minValue, maxValue);
    }

    public abstract T getDefaultMinValue ();

    public abstract T getDefaultMaxValue ();

    public abstract T parseNumber (String number);

    public abstract ConfigurationPropertyKey<T> createPropertyKey (String processedPropertyName, String category,
        Unit unit, T defaultValue, T minValue, T maxValue);

    /**
     * A property key generator for integer properties.
     * 
     * @author CraftedMods
     *
     */
    @Register(properties = AutoConfigurationConstants.PROPERTY_GENERATOR_TYPE_PROPERTY_KEY + "=java.lang.Integer")
    public static class IntegerKeyGenerator extends NumberPropertyKeyCreator<Integer>
    {

        @Override
        public Integer getDefaultMinValue ()
        {
            return Integer.MIN_VALUE;
        }

        @Override
        public Integer getDefaultMaxValue ()
        {
            return Integer.MAX_VALUE;
        }

        @Override
        public Integer parseNumber (String number)
        {
            return Integer.parseInt (number);
        }

        @Override
        public ConfigurationPropertyKey<Integer> createPropertyKey (String processedPropertyName, String category,
            Unit unit, Integer defaultValue, Integer minValue, Integer maxValue)
        {
            return new ConfigurationPropertyKey.IntegerKey (processedPropertyName, category, unit,
                defaultValue == null ? 0 : defaultValue, minValue, maxValue);
        }

    }

    /**
     * A property key generator for float properties.
     * 
     * @author CraftedMods
     *
     */
    @Register(properties = AutoConfigurationConstants.PROPERTY_GENERATOR_TYPE_PROPERTY_KEY + "=java.lang.Float")
    public static class FloatKeyGenerator extends NumberPropertyKeyCreator<Float>
    {

        @Override
        public Float getDefaultMinValue ()
        {
            return Float.MIN_VALUE;
        }

        @Override
        public Float getDefaultMaxValue ()
        {
            return Float.MAX_VALUE;
        }

        @Override
        public Float parseNumber (String number)
        {
            return Float.parseFloat (number);
        }

        @Override
        public ConfigurationPropertyKey<Float> createPropertyKey (String processedPropertyName, String category,
            Unit unit, Float defaultValue, Float minValue, Float maxValue)
        {
            return new ConfigurationPropertyKey.FloatKey (processedPropertyName, category, unit,
                defaultValue == null ? 0.0f : defaultValue, minValue, maxValue);
        }

    }

}
