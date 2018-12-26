package pvpmode.api.common.configuration;

import java.util.*;

import net.minecraftforge.common.util.EnumHelper;

/**
 * The basic class for configuration property keys. The class can be extended to
 * extend to functionality of the property keys.
 * 
 * @author CraftedMods
 *
 * @param <T>
 *            The value type the key holds
 */
public class ConfigurationPropertyKey<T>
{

    /**
     * An enum containing the units of the values assigned to the property keys.
     * Additional values can be created via the {@link EnumHelper}.
     * 
     * @author CraftedMods
     *
     */
    public static enum Unit
    {
    NONE, BLOCKS, ITEM_STACKS, SECONDS, TICKS;
    }

    public static final String CATEGORY_SEPARATOR = ".";

    protected final String internalName;
    protected final Class<T> valueType;
    protected final String category;
    protected final Unit unit;
    protected final T defaultValue;

    public ConfigurationPropertyKey (String internalName, Class<T> valueType, String category, Unit unit,
        T defaultValue)
    {
        Objects.requireNonNull (internalName);
        Objects.requireNonNull (valueType);
        Objects.requireNonNull (category);
        Objects.requireNonNull (unit);
        Objects.requireNonNull (defaultValue);

        this.internalName = internalName;
        this.valueType = valueType;
        this.category = category;
        this.unit = unit;
        this.defaultValue = defaultValue;
    }

    public ConfigurationPropertyKey (String name, Class<T> valueType, String category, T defaultValue)
    {
        this (name, valueType, category, Unit.NONE, defaultValue);
    }

    /**
     * Returns the internal name of the property key. It should be lowercase and
     * shouldn't contain spaces - a "_" can be used instead of them.
     * 
     * @return The internal name
     */
    public String getInternalName ()
    {
        return internalName;
    }

    /**
     * Returns the value type of the property key, to example String for a string
     * property key.
     * 
     * @return The value type
     */
    public Class<T> getValueType ()
    {
        return valueType;
    }

    /**
     * Returns the category the property key is assigned to. Child categories can be
     * appended with the {@link ConfigurationPropertyKey#CATEGORY_SEPARATOR}.
     * 
     * @return The category
     */
    public String getCategory ()
    {
        return category;
    }

    /**
     * Returns the unit of the property key, or null, if it hasn't an unit.
     * 
     * @return The unit
     */
    public Unit getUnit ()
    {
        return unit;
    }

    /**
     * Returns the default value of the property key. It mustn't be null.
     * 
     * @return The default value
     */
    public T getDefaultValue ()
    {
        return defaultValue;
    }

    /**
     * Returns whether the specified value is a valid one for the property.
     * 
     * @param value
     *            The value to test
     * @return Whether it's valid
     */
    public boolean isValidValue (T value)
    {
        return value == null ? false : value.equals (defaultValue) ? true : false;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (category == null ? 0 : category.hashCode ());
        result = prime * result + (defaultValue == null ? 0 : defaultValue.hashCode ());
        result = prime * result + (internalName == null ? 0 : internalName.hashCode ());
        result = prime * result + (unit == null ? 0 : unit.hashCode ());
        result = prime * result + (valueType == null ? 0 : valueType.hashCode ());
        return result;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass () != obj.getClass ())
            return false;
        ConfigurationPropertyKey<?> other = (ConfigurationPropertyKey<?>) obj;
        if (category == null)
        {
            if (other.category != null)
                return false;
        }
        else if (!category.equals (other.category))
            return false;
        if (defaultValue == null)
        {
            if (other.defaultValue != null)
                return false;
        }
        else if (!defaultValue.equals (other.defaultValue))
            return false;
        if (internalName == null)
        {
            if (other.internalName != null)
                return false;
        }
        else if (!internalName.equals (other.internalName))
            return false;
        if (unit != other.unit)
            return false;
        if (valueType == null)
        {
            if (other.valueType != null)
                return false;
        }
        else if (!valueType.equals (other.valueType))
            return false;
        return true;
    }

    @Override
    public String toString ()
    {
        return String.format ("Name: %s Type: %s Category: %s Unit: %s Default Value: %s", internalName,
            valueType.getName (),
            category,
            unit != null && unit != Unit.NONE ? unit.toString () : "None", defaultValue);
    }

    /**
     * An extension of the configuration property key class with the boolean value
     * type.
     * 
     * @author CraftedMods
     *
     */
    public static class BooleanKey extends ConfigurationPropertyKey<Boolean>
    {

        public BooleanKey (String name, String category, boolean defaultValue)
        {
            super (name, Boolean.class, category, defaultValue);
        }

        public BooleanKey (String name, String category)
        {
            this (name, category, false);
        }

        @Override
        public boolean isValidValue (Boolean value)
        {
            return super.isValidValue (value) || value != null;
        }

    }

    /**
     * An abstract extension of the configuration property key class for numbers.
     * 
     * @author CraftedMods
     *
     * @param <T>
     *            The number type
     */
    public static abstract class AbstractNumberKey<T extends Number> extends ConfigurationPropertyKey<T>
        implements Comparator<T>
    {

        protected final T minValue;
        protected final T maxValue;

        public AbstractNumberKey (String name, Class<T> numberClass, String category, Unit unit, T defaultValue,
            T minValue,
            T maxValue)
        {
            super (name, numberClass, category, unit, defaultValue);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public AbstractNumberKey (String name, Class<T> numberClass, String category, T defaultValue, T minValue,
            T maxValue)
        {
            this (name, numberClass, category, Unit.NONE, defaultValue, minValue, maxValue);
        }

        /**
         * Returns the minimum valid value
         * 
         * @return The minimum valid value
         */
        public T getMinValue ()
        {
            return minValue;
        }

        /**
         * Returns the maximum valid value
         * 
         * @return The maximum valid value
         */
        public T getMaxValue ()
        {
            return maxValue;
        }

        @Override
        public boolean isValidValue (T value)
        {
            return super.isValidValue (value) || this.compare (value, minValue) >= 0
                || this.compare (value, maxValue) <= 0;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = super.hashCode ();
            result = prime * result + (maxValue == null ? 0 : maxValue.hashCode ());
            result = prime * result + (minValue == null ? 0 : minValue.hashCode ());
            return result;
        }

        @Override
        public boolean equals (Object obj)
        {
            if (this == obj)
                return true;
            if (!super.equals (obj))
                return false;
            if (getClass () != obj.getClass ())
                return false;
            AbstractNumberKey<?> other = (AbstractNumberKey<?>) obj;
            if (maxValue == null)
            {
                if (other.maxValue != null)
                    return false;
            }
            else if (!maxValue.equals (other.maxValue))
                return false;
            if (minValue == null)
            {
                if (other.minValue != null)
                    return false;
            }
            else if (!minValue.equals (other.minValue))
                return false;
            return true;
        }

        @Override
        public String toString ()
        {
            return String.format ("%s Min Value: %s Max Value: %s", super.toString (), minValue, maxValue);
        }

    }

    /**
     * An extension of the configuration property key class for integers.
     * 
     * @author CraftedMods
     *
     */
    public static class IntegerKey extends AbstractNumberKey<Integer>
    {

        public IntegerKey (String name, String category, Unit unit, Integer defaultValue, Integer minValue,
            Integer maxValue)
        {
            super (name, Integer.class, category, unit, defaultValue, minValue, maxValue);
        }

        public IntegerKey (String name, String category, Integer defaultValue, Integer minValue, Integer maxValue)
        {
            super (name, Integer.class, category, defaultValue, minValue, maxValue);
        }

        public IntegerKey (String name, String category, Unit unit, Integer defaultValue)
        {
            super (name, Integer.class, category, unit, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public IntegerKey (String name, String category, Integer defaultValue)
        {
            super (name, Integer.class, category, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public IntegerKey (String name, String category, Unit unit)
        {
            super (name, Integer.class, category, unit, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public IntegerKey (String name, String category)
        {
            super (name, Integer.class, category, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        @Override
        public int compare (Integer number1, Integer number2)
        {
            return number1.compareTo (number2);
        }

    }

    /**
     * An extension of the configuration property key class for floats.
     * 
     * @author CraftedMods
     *
     */
    public static class FloatKey extends AbstractNumberKey<Float>
    {

        public FloatKey (String name, String category, Unit unit, Float defaultValue,
            Float minValue,
            Float maxValue)
        {
            super (name, Float.class, category, unit, defaultValue, minValue, maxValue);
        }

        public FloatKey (String name, String category, Float defaultValue, Float minValue,
            Float maxValue)
        {
            super (name, Float.class, category, defaultValue, minValue, maxValue);
        }

        public FloatKey (String name, String category, Unit unit, Float defaultValue)
        {
            super (name, Float.class, category, unit, defaultValue, Float.MIN_VALUE,
                Float.MAX_VALUE);
        }

        public FloatKey (String name, String category, Float defaultValue)
        {
            super (name, Float.class, category, defaultValue, Float.MIN_VALUE,
                Float.MAX_VALUE);
        }

        public FloatKey (String name, String category, Unit unit)
        {
            super (name, Float.class, category, unit, 0f, Float.MIN_VALUE,
                Float.MAX_VALUE);
        }

        public FloatKey (String name, String category)
        {
            super (name, Float.class, category, 0f, Float.MIN_VALUE, Float.MAX_VALUE);
        }

        @Override
        public int compare (Float number1, Float number2)
        {
            return number1.compareTo (number2);
        }

    }

    /**
     * An extension of the configuration property key class for strings.
     * 
     * @author CraftedMods
     *
     */
    public static class StringKey extends ConfigurationPropertyKey<String>
    {

        public StringKey (String name, String category, String defaultValue)
        {
            super (name, String.class, category, defaultValue);
        }

    }

    /**
     * An extension of the configuration property key class for integers.
     *
     * @author CraftedMods
     *
     * @param <T>
     *            The contained value type
     * @param <C>
     *            The valid value type
     */
    public static abstract class ValidValuesHolder<T, C> extends ConfigurationPropertyKey<C>
    {

        protected final Collection<T> validValues;

        public ValidValuesHolder (String name, Class<C> valueType, String category, Unit unit, C defaultValue,
            Collection<T> validValues)
        {
            super (name, valueType, category, unit, defaultValue);
            this.validValues = validValues;
        }

        public ValidValuesHolder (String name, Class<C> valueType, String category, C defaultValue,
            Collection<T> validValues)
        {
            this (name, valueType, category, Unit.NONE, defaultValue, validValues);
        }

        public ValidValuesHolder (String name, Class<C> valueType, String category, Unit unit, C defaultValue)
        {
            this (name, valueType, category, unit, defaultValue, null);
        }

        public ValidValuesHolder (String name, Class<C> valueType, String category, C defaultValue)
        {
            this (name, valueType, category, Unit.NONE, defaultValue, null);
        }

        /**
         * Returns a collection of valid values. Can be null, if no valid values were
         * specified.
         * 
         * @return A collection of valid values
         */
        public Collection<T> getValidValues ()
        {
            return validValues;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = super.hashCode ();
            result = prime * result + (validValues == null ? 0 : validValues.hashCode ());
            return result;
        }

        @Override
        public boolean equals (Object obj)
        {
            if (this == obj)
                return true;
            if (!super.equals (obj))
                return false;
            if (getClass () != obj.getClass ())
                return false;
            ValidValuesHolder<?, ?> other = (ValidValuesHolder<?, ?>) obj;
            if (validValues == null)
            {
                if (other.validValues != null)
                    return false;
            }
            else if (!validValues.equals (other.validValues))
                return false;
            return true;
        }

    }

    /**
     * 
     * An extension of the configuration property key class specifically for string
     * collections.
     * 
     * @author CraftedMods
     *
     * @param <T>
     *            The string collection type
     */
    public static abstract class AbstractStringCollectionKey<T extends Collection<String>>
        extends ValidValuesHolder<String, T>
    {

        public AbstractStringCollectionKey (String name, Class<T> collectionClass, String category, T defaultValue,
            T validValues)
        {
            super (name, collectionClass, category, defaultValue, validValues);
        }

        @Override
        public boolean isValidValue (T value)
        {
            boolean isValid = super.isValidValue (value);
            if (!isValid && validValues != null)
            {
                for (String string : value)
                {
                    if (validValues.contains (string))
                        return true;

                }
            }
            return isValid;
        }

    }

    /**
     * An extension of the configuration property key class for string lists.
     * 
     * @author CraftedMods
     *
     */
    public static class StringList extends ConfigurationPropertyKey.AbstractStringCollectionKey<List<String>>
    {

        @SuppressWarnings("unchecked")
        public StringList (String name, String category, List<String> defaultValue, List<String> validValues)
        {
            super (name, (Class<List<String>>) (Class<?>) List.class, category, defaultValue, validValues);
        }

        public StringList (String name, String category, List<String> defaultValue)
        {
            this (name, category, defaultValue, null);
        }

        public StringList (String name, String category)
        {
            this (name, category, Arrays.asList (), null);
        }

    }

    /**
     * An extension of the configuration property key class for string sets.
     * 
     * @author CraftedMods
     *
     */
    public static class StringSet extends ConfigurationPropertyKey.AbstractStringCollectionKey<Set<String>>
    {

        @SuppressWarnings("unchecked")
        public StringSet (String name, String category, Set<String> defaultValue, Set<String> validValues)
        {
            super (name, (Class<Set<String>>) (Class<?>) Set.class, category, defaultValue, validValues);
        }

        public StringSet (String name, String category, Set<String> defaultValue)
        {
            this (name, category, defaultValue, null);
        }

        public StringSet (String name, String category)
        {
            this (name, category, new HashSet<> (), null);
        }

    }

    /**
     * A generic extension of the configuration property key class for enums.
     * 
     * @author CraftedMods
     *
     */
    public static class EnumKey<T extends Enum<T>> extends ValidValuesHolder<T, T>
    {

        public EnumKey (String name, Class<T> enumClass, String category, Unit unit, T defaultValue)
        {
            super (name, enumClass, category, unit, defaultValue, Arrays.asList (enumClass.getEnumConstants ()));
        }

        public EnumKey (String name, Class<T> enumClass, String category, T defaultValue)
        {
            super (name, enumClass, category, defaultValue, Arrays.asList (enumClass.getEnumConstants ()));
        }

        public EnumKey (String name, Class<T> enumClass, String category, Unit unit, T defaultValue,
            Collection<T> validValues)
        {
            super (name, enumClass, category, unit, defaultValue, validValues);
        }

        public EnumKey (String name, Class<T> enumClass, String category, T defaultValue, Collection<T> validValues)
        {
            super (name, enumClass, category, defaultValue, validValues);
        }

        @Override
        public boolean isValidValue (T value)
        {
            boolean isValid = super.isValidValue (value);
            if (!isValid && validValues != null && validValues.contains (value))
                return true;
            return isValid;
        }

    }

}
