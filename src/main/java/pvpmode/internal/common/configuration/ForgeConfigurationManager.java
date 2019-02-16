package pvpmode.internal.common.configuration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;

import net.minecraftforge.common.config.*;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.*;

/**
 * A configuration manager using the MinecraftForge configuration system.
 * 
 * @author CraftedMods
 *
 */
public abstract class ForgeConfigurationManager extends AbstractConfigurationManager
{

    protected final Configuration configuration;

    protected final SimpleLogger logger;

    private boolean wasSavedWithFirstLoading = false;

    protected ForgeConfigurationManager (Configuration configuration, SimpleLogger logger)
    {
        this.configuration = configuration;
        this.logger = logger;
    }

    @Override
    public void load ()
    {
        configuration.load ();
        super.load ();

        if (!wasSavedWithFirstLoading)
        {
            this.save (); // If the configuration is created for the first time, the configuration file
                          // has to be written with this method
            wasSavedWithFirstLoading = true;
        }
    }

    @Override
    public void save ()
    {
        if (configuration.hasChanged ())
        {
            configuration.save ();
        }
    }

    @SuppressWarnings(
    {"unchecked", "rawtypes"})
    @Override
    public <T> boolean setProperty (ConfigurationPropertyKey<T> key, T newValue)
    {
        if (super.setProperty (key, newValue))
        {
            String[] categoryParts = key.getCategory ().split ("\\" + ConfigurationPropertyKey.CATEGORY_SEPARATOR);

            ConfigCategory currentCategory = null;
            Map<String, ConfigCategory> childCategories = this.configuration.getCategoryNames ().stream ()
                .collect (Collectors.toMap (Function.identity (), configuration::getCategory));

            for (int i = 0; i < categoryParts.length; i++)
            {
                String part = categoryParts[i];

                if (childCategories.containsKey (part))
                {
                    currentCategory = childCategories.get (part);
                    childCategories = currentCategory.getChildren ().stream ()
                        .collect (Collectors.toMap (category -> category.getName (), Function.identity ()));
                }
                else
                {
                    logger.error (
                        "Cannot set the new property value: The category \"%s\" of the configuration property key \"%s\" is not a valid one. This is an internal error, because this should never happen.",
                        key.getCategory (),
                        key.getInternalName ());
                    return false;
                }
            }

            if (currentCategory != null)
            {

                String propertyName = this.getNameWithUnit (key);
                if (currentCategory.containsKey (propertyName))
                {
                    Property property = currentCategory.get (propertyName);

                    if (Boolean.class.isAssignableFrom (key.getValueType ()))
                    {
                        property.set ((Boolean) newValue);
                    }
                    else if (Enum.class.isAssignableFrom (key.getValueType ()))
                    {
                        property.set ( ((Enum) newValue).name ());
                    }
                    else if (key instanceof IntegerKey)
                    {
                        property.set ((Integer) newValue);
                    }
                    else if (key instanceof FloatKey)
                    {
                        property.set ((Float) newValue);
                    }
                    else if (String.class.isAssignableFrom (key.getValueType ()))
                    {
                        property.set ((String) newValue);
                    }
                    else if (key instanceof StringList)
                    {
                        List<String> stringList = (List<String>) newValue;
                        property.set (stringList.toArray (new String[stringList.size ()]));
                    }
                    else if (key instanceof StringSet)
                    {
                        Set<String> stringSet = (Set<String>) newValue;
                        property.set (stringSet.toArray (new String[stringSet.size ()]));
                    }
                    else if (key instanceof StringMap)
                    {
                        Map<String, String> stringMap = (Map<String, String>) newValue;
                        property.set (StringMap.toStringList (stringMap).toArray (new String[stringMap.size ()]));
                    }
                    else if (key instanceof StringMultimap)
                    {
                        Multimap<String, String> stringMultimap = (Multimap<String, String>) newValue;
                        property.set (StringMap.toStringList (StringMultimap.toStringMap (stringMultimap))
                            .toArray (new String[stringMultimap.keySet ().size ()]));
                    }
                    else
                    {
                        logger.warning (
                            "Cannot set the new property value: The type \"%s\" of the configuration property key \"%s\" is not supported, though it seems that this property is registered",
                            key.getValueType (),
                            key.getInternalName ());
                        return false;
                    }
                    this.save ();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<ConfigurationPropertyKey<?>, Object> retrieveProperties ()
    {
        Map<ConfigurationPropertyKey<?>, Object> ret = new HashMap<> ();
        for (ConfigurationPropertyKey<?> key : getRegisteredPropertyKeys ().values ())
        {
            if (Boolean.class.isAssignableFrom (key.getValueType ()))
            {
                ret.put (key,
                    getBoolean ((ConfigurationPropertyKey<Boolean>) key, (Boolean) key.getDefaultValue (),
                        getComment (key)));
            }
            else if (Enum.class.isAssignableFrom (key.getValueType ()))
            {
                ret.put (key,
                    this.getEnum ((ConfigurationPropertyKey<? extends Enum<?>>) key,
                        (Enum<?>) key.getDefaultValue ()));
            }
            else if (key instanceof IntegerKey)
            {
                IntegerKey intKey = (IntegerKey) key;
                ret.put (intKey,
                    getInt (intKey, intKey.getDefaultValue (), intKey.getMinValue (), intKey.getMaxValue (),
                        getComment (key)));
            }
            else if (key instanceof FloatKey)
            {
                FloatKey floatKey = (FloatKey) key;
                ret.put (floatKey,
                    getFloat (floatKey, floatKey.getDefaultValue (), floatKey.getMinValue (),
                        floatKey.getMaxValue (),
                        getComment (key)));
            }
            else if (String.class.isAssignableFrom (key.getValueType ()))
            {
                ret.put (key, getString ((ConfigurationPropertyKey<String>) key, (String) key.getDefaultValue (),
                    getComment (key)));
            }
            else if (key instanceof StringList)
            {
                ret.put (key,
                    getStringList ((ConfigurationPropertyKey<List<String>>) key,
                        (List<String>) key.getDefaultValue (),
                        (List<String>) ((StringList) key).getValidValues (), getComment (key)));
            }
            else if (key instanceof StringSet)
            {
                ret.put (key,
                    getStringSet ((ConfigurationPropertyKey<Set<String>>) key,
                        (Set<String>) key.getDefaultValue (),
                        (Set<String>) ((StringSet) key).getValidValues (), getComment (key)));
            }
            else if (key instanceof StringMap)
            {
                ret.put (key, getStringMap ((StringMap) key, (Map<String, String>) key.getDefaultValue (),
                    (Collection<Map.Entry<String, String>>) ((StringMap) key).getValidValues (), getComment (key)));
            }
            else if (key instanceof StringMultimap)
            {
                ret.put (key,
                    getStringMultimap ((StringMultimap) key, (Multimap<String, String>) key.getDefaultValue (),
                        (Collection<Map.Entry<String, Collection<String>>>) ((StringMultimap) key).getValidValues (),
                        getComment (key)));
            }
            else
            {
                logger.warning ("The type \"%s\" of the configuration property key \"%s\" is not supported",
                    key.getValueType (),
                    key.getInternalName ());
            }
        }
        return ret;
    }

    protected abstract Map<String, ConfigurationPropertyKey<?>> getRegisteredPropertyKeys ();

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

    @SuppressWarnings("unchecked")
    protected <T extends Enum<?>, J extends Enum<J>> Enum<?> getEnum (ConfigurationPropertyKey<T> key,
        Enum<?> defaultValue)
    {
        return this.getEnum ((ConfigurationPropertyKey.EnumKey<J>) (ConfigurationPropertyKey<?>) key, (J) defaultValue,
            getComment (key));
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
    protected List<String> getFixedSizeStringList (ConfigurationPropertyKey<?> key,
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
     * Gets or creates the string map property assigned to the specified property
     * key, with the specified default values, valid values and comment. The valid
     * values can be null.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValues
     *            The default content of the map
     * @param validValues
     *            The valid values of the map
     * @param comment
     *            The comment of the property
     * @return The string map property value
     */
    protected Map<String, String> getStringMap (ConfigurationPropertyKey<?> key,
        Map<String, String> defaultValues,
        Collection<Map.Entry<String, String>> validValues,
        String comment)
    {
        List<String> parsedDefaultEntries = defaultValues.entrySet ().stream ()
            .map (entry -> entry.getKey () + "=" + entry.getValue ()).collect (Collectors.toList ());
        List<String> parsedValidEntries = validValues != null ? validValues.stream ()
            .map (entry -> entry.getKey () + "=" + entry.getValue ()).collect (Collectors.toList ()) : null;

        List<String> unparsedEntries = getFixedSizeStringList (key, parsedDefaultEntries, parsedValidEntries,
            comment);

        return StringMap.fromStringList (unparsedEntries);
    }

    /**
     * Gets or creates the string multimap property assigned to the specified
     * property key, with the specified default values, valid values and comment.
     * The valid values can be null.
     * 
     * @param key
     *            The configuration property key
     * @param defaultValues
     *            The default content of the multimap
     * @param validValues
     *            The valid values of the multimap
     * @param comment
     *            The comment of the property
     * @return The string map property value
     */
    protected Map<String, String> getStringMultimap (ConfigurationPropertyKey<Multimap<String, String>> key,
        Multimap<String, String> defaultValues,
        Collection<Map.Entry<String, Collection<String>>> validValues,
        String comment)
    {
        return this.getStringMap (key, StringMultimap.toStringMap (defaultValues),
            validValues != null
                ? validValues.stream ()
                    .map (entry -> Pair.of (entry.getKey (), StringList.asString (entry.getValue ())))
                    .collect (Collectors.toList ())
                : null,
            comment);
    }

    /**
     * Returns the display name of the supplied configuration property key.
     * 
     * @param key
     *            The property key
     * @return The display name of this key
     */
    public String getDisplayName (ConfigurationPropertyKey<?> key)
    {
        return key.getInternalName ();
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
        Unit unit = key.getUnit ();
        String name = getDisplayName (key);
        return unit != null && unit != Unit.NONE
            ? String.format ("%s (in %s)", name,
                unit.name ().toLowerCase ().replaceAll ("_", " "))
            : name;
    }

    /**
     * Returns the comment for the supplied configuration property key.
     * 
     * @param key
     *            The property key
     * @return The comment of this key
     */
    public String getComment (ConfigurationPropertyKey<?> key)
    {
        return "";
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
