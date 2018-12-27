package pvpmode.internal.common.configuration;

import java.util.*;

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
        properties.clear ();
        properties.putAll (retrieveProperties ());
        properties.keySet ().forEach (key -> propertyKeys.put (key.getInternalName (), key));
        unmodifiablePropertyKeys = Collections.unmodifiableMap (propertyKeys);

        createCategoryHierarchy ();
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

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty (ConfigurationPropertyKey<T> key)
    {
        return (T) properties.get (key);
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
