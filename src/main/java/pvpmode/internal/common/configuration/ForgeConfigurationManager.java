package pvpmode.internal.common.configuration;

import java.util.*;

import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;

/**
 * A configuration manager using the MinecraftForge configuration system.
 * 
 * @author CraftedMods
 *
 */
public abstract class ForgeConfigurationManager extends AbstractConfigurationManager
{

    protected final Configuration configuration;

    protected ForgeConfigurationManager (Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void load ()
    {
        configuration.load ();
        super.load ();

        this.save (); // If the configuration is created for the first time, the configuration file
                      // has to be written with this method
    }

    @Override
    public void save ()
    {
        if (configuration.hasChanged ())
        {
            configuration.save ();
        }
    }

    /**
     * Gets or creates a string property assigned to the specified property key,
     * with the specified default, and a comment.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValue
     *            The default value
     * @param comment
     *            The comment
     * @return The string property value
     */
    protected String getString (ConfigurationPropertyKey<String> key, String defaultValue, String comment)
    {
        return configuration.getString (this.getNameWithUnit (key), key.getCategory (), defaultValue,
            comment);
    }

    /**
     * Get or creates an enum property assigned to the specified property key, with
     * the specified default value. Valid values for this property are the enum
     * values.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValue
     *            The default value
     * @param comment
     *            The comment
     * @return The enum property value
     */
    protected <T extends Enum<T>> T getEnum (ConfigurationPropertyKey.EnumKey<T> key, T defaultValue,
        String comment)
    {
        return Enum.valueOf (key.getValueType (),
            configuration.getString (this.getNameWithUnit (key), key.getCategory (),
                defaultValue.toString (),
                getCommentWithValidValues (comment, key.getValidValues ())));
    }

    /**
     * Gets or creates a boolean property assigned to the specified property key,
     * with the specified default, and a comment.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValue
     *            The default value
     * @param comment
     *            The comment
     * @return The boolean property value
     */
    protected boolean getBoolean (ConfigurationPropertyKey<Boolean> key, boolean defaultValue, String comment)
    {
        return configuration.getBoolean (this.getNameWithUnit (key), key.getCategory (), defaultValue,
            comment);
    }

    /**
     * Gets or creates an integer property assigned to the specified property key,
     * with the specified default, minimum and maximum values, and a comment.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValue
     *            The default value
     * @param minValue
     *            The minimum value
     * @param maxValue
     *            The maximum value
     * @param comment
     *            The comment
     * @return The integer property value
     */
    protected int getInt (ConfigurationPropertyKey<Integer> key, int defaultValue, int minValue, int maxValue,
        String comment)
    {
        return configuration.getInt (this.getNameWithUnit (key), key.getCategory (), defaultValue,
            minValue, maxValue,
            comment);
    }

    /**
     * Gets or creates a float property assigned to the specified property key, with
     * the specified default, minimum and maximum values, and a comment.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValue
     *            The default value
     * @param minValue
     *            The minimum value
     * @param maxValue
     *            The maximum value
     * @param comment
     *            The comment
     * @return The float property value
     */
    protected float getFloat (ConfigurationPropertyKey<Float> key, float defaultValue, float minValue, float maxValue,
        String comment)
    {
        return configuration.getFloat (this.getNameWithUnit (key), key.getCategory (), defaultValue,
            minValue, maxValue,
            comment);
    }

    /**
     * Gets or creates the string list property assigned to the specified property
     * key, with the specified default values, valid values and comment. The valid
     * values can be null. The returned list has a fixed size.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValues
     *            The default content of the list
     * @param validValues
     *            The valid values of the list
     * @param comment
     *            The comment of the property
     * @return The string list property value
     */
    protected List<String> getFixedSizeStringList (ConfigurationPropertyKey<? extends Collection<String>> key,
        List<String> defaultValues,
        List<String> validValues,
        String comment)
    {
        return Arrays.asList (configuration.getStringList (getNameWithUnit (key), key.getCategory (),
            defaultValues.toArray (new String[defaultValues.size ()]), getCommentWithValidValues (comment, validValues),
            validValues == null ? new String[] {} : validValues.toArray (new String[validValues.size ()])));
    }

    /**
     * Gets or creates the string list property assigned to the specified property
     * key, with the specified default values, valid values and comment. The valid
     * values can be null.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValues
     *            The default content of the list
     * @param validValues
     *            The valid values of the list
     * @param comment
     *            The comment of the property
     * @return The string list property value
     */
    protected List<String> getStringList (ConfigurationPropertyKey<List<String>> key, List<String> defaultValues,
        List<String> validValues,
        String comment)
    {
        return new ArrayList<> (getFixedSizeStringList (key, defaultValues, validValues,
            comment));
    }

    /**
     * Gets or creates the string set property assigned to the specified property
     * key, with the specified default values, valid values and comment. The valid
     * values can be null.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValues
     *            The default content of the set
     * @param validValues
     *            The valid values of the set
     * @param comment
     *            The comment of the property
     * @return The string set property value
     */
    protected Set<String> getStringSet (ConfigurationPropertyKey<Set<String>> key, Set<String> defaultValues,
        Set<String> validValues,
        String comment)
    {
        return new HashSet<> (
            getFixedSizeStringList (key, new ArrayList<> (defaultValues),
                validValues == null ? null : new ArrayList<> (validValues), comment));
    }

    /**
     * Returns the property key name appended with the unit of this key. Subclasses
     * can override this function to use a property key name more suitable for
     * display purposed than the internal name.
     * 
     * @param key
     *            The property key
     * @return The property key name with the unit
     */
    protected String getNameWithUnit (ConfigurationPropertyKey<?> key)
    {
        return getNameWithUnit (key.getInternalName (), key.getUnit ());
    }

    /**
     * Returns a name and appends the unit, if specified. For display purposes.
     * 
     * @param name
     *            A name
     * @param unit
     *            The unit
     * @return The name with the unit
     */
    protected String getNameWithUnit (String name, Unit unit)
    {
        return unit != null && unit != Unit.NONE
            ? String.format ("%s (in %s)", name,
                unit.name ().toLowerCase ().replaceAll ("_", " "))
            : name;
    }

    /**
     * Appends the valid values to the specified comment.
     * 
     * @param comment
     *            The comment
     * @param validValues
     *            The valid values
     * @return The comment with the valid values appended
     */
    protected String getCommentWithValidValues (String comment, Collection<?> validValues)
    {
        return validValues != null ? String.format ("%s [valid: %s]", comment, validValues) : comment;
    }

}
