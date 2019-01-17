package pvpmode.api.common.configuration;

import java.util.Map;

/**
 * The parent interface all configuration managers have to implement, it gives
 * them the general abilities to handle configuration data.
 * 
 * @author CraftedMods
 *
 */
public interface ConfigurationManager
{

    /**
     * Persists the currently loaded configuration data, and overrides the
     * persistent ones.
     */
    public void save ();

    /**
     * Replaces the loaded configuration data with the persistent ones.
     */
    public void load ();

    /**
     * Returns the configuration property value matching to the specified key, or
     * null, if no such key was found.
     * 
     * @param key
     *            The configuration property key
     * @return The value assigned to the key
     */
    public <T> T getProperty (ConfigurationPropertyKey<T> key);

    /**
     * Sets the value of the property key to the specified one.
     * {@link ConfigurationPropertyKey#isValidValue(Object)} has to return true for
     * the specified value so that it can replace the old one. The method returns
     * whether the property was actually modified, so it returns false, if the
     * property isn't modifiable, the supplied value wasn't valid or the supplied
     * value was the same as the current one.
     * 
     * @param key
     *            The configuration property key
     * @param value
     *            The new value for the property
     * @return Whether the property value could be changed
     */
    public <T> boolean setProperty (ConfigurationPropertyKey<T> key, T value);

    /**
     * Returns a map containing all configuration property keys assigned to this
     * configuration manager. The key of the map is the internal property key name,
     * the value is the actual key instance.
     * 
     * @return A map containing the property keys
     */
    public Map<String, ConfigurationPropertyKey<?>> getPropertyKeys ();

    /**
     * Returns an object graph representing the configuration hierarchy, with
     * categories containing child-categories and their properties, starting with
     * the root categories. The keys of the map are the internal category names.
     * 
     * @return The root category map
     */
    public Map<String, Category> getRootCategories ();

    /**
     * Returns the category object, which represents the category the supplied
     * property key is assigned to.
     * 
     * @param key
     *            The property which category should be determined
     * @return The category object
     */
    public Category getCategory (ConfigurationPropertyKey<?> key);

    /**
     * An object representing a configuration category. It will be implemented by
     * the providers, normal users shouldn't implement this interface.
     *
     * @author CraftedMods
     *
     */
    public interface Category
    {

        /**
         * Returns the simple, internal, unlocalized category name.
         *
         * @return The simple category name
         */
        public String getInternalName ();

        /**
         * Returns the full, internal, unlocalized category name. It starts with the
         * root category, then come the parent categories, separated with the dot and at
         * last the simple, internal category name.
         *
         * @return The full category name
         */
        public String getFullName ();

        /**
         * Returns the parent category of this category or null, if it's a root
         * category.
         *
         * @return The parent category
         */
        public Category getParentCategory ();

        /**
         * Returns true if the parent category is null, otherwise false.
         *
         * @return Whether the parent category is null
         */
        public default boolean isRootCategory ()
        {
            return getParentCategory () == null;
        }

        /**
         * Returns a collections containing the child categories or an empty collection
         * of this category has none.
         *
         * @return The child categories
         */
        public Map<String, Category> getSubcategories ();

        /**
         * Returns the property keys of the properties assigned to this category, or an
         * empty collection of there're none.
         *
         * @return The configuration properties
         */
        public Map<String, ConfigurationPropertyKey<?>> getProperties ();
    }

}
