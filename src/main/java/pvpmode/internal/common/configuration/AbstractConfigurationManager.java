package pvpmode.internal.common.configuration;

import java.util.*;

import net.minecraftforge.common.MinecraftForge;
import pvpmode.api.common.configuration.*;

/**
 * A basic implementation of a configuration manager.
 *
 * @author CraftedMods
 *
 */
public abstract class AbstractConfigurationManager implements ConfigurationManager
{

    protected final Map<ConfigurationPropertyKey<?>, Object> properties = new HashMap<> ();
    protected final Map<String, ConfigurationPropertyKey<?>> propertyKeys = new HashMap<> ();
    protected final Map<String, Category> rootCategories = new HashMap<> ();

    private Map<String, ConfigurationPropertyKey<?>> unmodifiablePropertyKeys;
    private Map<String, Category> unmodifiableRootCategories;

    @Override
    public void load ()
    {
        Map<ConfigurationPropertyKey<?>, Object> oldProperties = new HashMap<> (properties);

        properties.clear ();
        properties.putAll (retrieveProperties ());
        properties.keySet ().forEach (key -> propertyKeys.put (key.getInternalName (), key));
        unmodifiablePropertyKeys = Collections.unmodifiableMap (propertyKeys);

        createCategoryHierarchy ();

        // Fire the property changed event for every property that changed after the
        // reload
        properties.forEach ( (key, newValue) ->
        {
            Object oldValue = oldProperties.get (key);
            if (!newValue.equals (oldValue))
            {
                this.onPropertyChanged (key, oldValue, newValue);
            }
        });

        this.onPropertiesChanged ();
    }

    private void createCategoryHierarchy ()
    {
        Map<String, CategoryImpl> categories = new HashMap<> ();

        for (ConfigurationPropertyKey<?> key : properties.keySet ())
        {
            String[] categoryParts = key.getCategory ()
                .split ("\\" + ConfigurationPropertyKey.CATEGORY_SEPARATOR);

            CategoryImpl parentCategory = null;
            Map<String, CategoryImpl> categoryMap = categories;

            for (int j = 0; j < categoryParts.length; j++)
            {
                String categoryPart = categoryParts[j];

                if (!categoryMap.containsKey (categoryPart))
                {
                    categoryMap.put (categoryPart, new CategoryImpl (categoryPart, parentCategory));
                }

                parentCategory = categoryMap.get (categoryPart);
                categoryMap = categoryMap.get (categoryPart).getModifiableSubcategories ();

                if (j == (categoryParts.length - 1))
                {
                    parentCategory.getModifiableProperties ().put (key.getInternalName (), key);
                }
            }
        }

        rootCategories.clear ();
        rootCategories.putAll (categories);
        unmodifiableRootCategories = Collections.unmodifiableMap (rootCategories);

    }

    /**
     * This function will be called upon loading to determine the currently relevant
     * property keys.
     *
     * @return The property keys of the manager
     */
    protected abstract Map<? extends ConfigurationPropertyKey<?>, Object> retrieveProperties ();

    /**
     * Called when the value of the supplied property was changed, or when the property
     * was initialized for the first time. In that case the old value is null.
     * 
     * @param key
     *            The configuration property key
     * @param oldValue
     *            The old property value
     * @param newValue
     *            The new property value
     */
    protected void onPropertyChanged (ConfigurationPropertyKey<?> key, Object oldValue, Object newValue)
    {
        MinecraftForge.EVENT_BUS.post (new OnConfigurationPropertyChangedEvent (key, newValue, oldValue));
    }

    /**
     * A fuunction that will be called after
     * {@link AbstractConfigurationManager#onPropertyChanged(ConfigurationPropertyKey, Object, Object)}
     * has been called for all properties that have been changed.
     */
    protected void onPropertiesChanged ()
    {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty (ConfigurationPropertyKey<T> key)
    {
        return (T) properties.get (key);
    }

    @Override
    public <T> boolean setProperty (ConfigurationPropertyKey<T> key, T newValue)
    {
        Object oldValue = properties.get (key);
        if (key.isValidValue (newValue) && !newValue.equals (oldValue))
        {
            properties.replace (key, newValue);
            this.onPropertyChanged (key, newValue, oldValue);
            this.onPropertiesChanged ();
            return true;
        }
        return false;
    }

    @Override
    public Map<String, ConfigurationPropertyKey<?>> getPropertyKeys ()
    {
        return unmodifiablePropertyKeys;
    }

    @Override
    public Map<String, Category> getRootCategories ()
    {
        return unmodifiableRootCategories;
    }

    @Override
    public Category getCategory (ConfigurationPropertyKey<?> key)
    {
        if (!properties.containsKey (key))
            throw new IllegalArgumentException (
                String.format ("The configuration property key \"%s\" is not assigned to this configuration manager",
                    key.getInternalName ()));

        String[] categoryParts = key.getCategory ().split ("\\" + ConfigurationPropertyKey.CATEGORY_SEPARATOR);

        Category category = null;
        Map<String, Category> categoryMap = rootCategories;

        for (String categoryPart : categoryParts)
        {
            category = categoryMap.get (categoryPart);
            categoryMap = category.getSubcategories ();
        }

        return category;
    }

    private class CategoryImpl implements Category
    {

        private final String internalName;
        private final Category parentCategory;
        private final Map<String, CategoryImpl> subcategories;
        private final Map<String, ConfigurationPropertyKey<?>> properties;

        private final Map<String, Category> unmodifiableSubcategories;
        private final Map<String, ConfigurationPropertyKey<?>> unmodifiableProperties;

        public CategoryImpl (String internalName, Category parentCategory)
        {
            this (internalName, parentCategory, new HashMap<> (), new HashMap<> ());
        }

        public CategoryImpl (String internalName, Category parentCategory,
            Map<String, CategoryImpl> subcategories,
            Map<String, ConfigurationPropertyKey<?>> properties)
        {
            this.internalName = internalName;
            this.parentCategory = parentCategory;
            this.subcategories = subcategories;
            this.properties = properties;

            this.unmodifiableSubcategories = Collections.unmodifiableMap (subcategories);
            this.unmodifiableProperties = Collections.unmodifiableMap (properties);
        }

        @Override
        public String getInternalName ()
        {
            return internalName;
        }

        @Override
        public String getFullName ()
        {
            return parentCategory == null ? this.getInternalName ()
                : parentCategory.getFullName () + ConfigurationPropertyKey.CATEGORY_SEPARATOR + this.getInternalName ();
        }

        @Override
        public Category getParentCategory ()
        {
            return parentCategory;
        }

        private Map<String, CategoryImpl> getModifiableSubcategories ()
        {
            return subcategories;
        }

        @Override
        public Map<String, Category> getSubcategories ()
        {
            return unmodifiableSubcategories;
        }

        @Override
        public Map<String, ConfigurationPropertyKey<?>> getProperties ()
        {
            return unmodifiableProperties;
        }

        private Map<String, ConfigurationPropertyKey<?>> getModifiableProperties ()
        {
            return properties;
        }

    }

}
