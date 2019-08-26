package pvpmode.api.common.utils;

import java.util.*;
import java.util.function.*;

import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;

/**
 * An utility class containing mostly core-modding related functions. A separate
 * class is used, so that the core-mod doesn't load game-logic related stuff.
 * All functions here can safely be called from the coremodding infrastructure.
 * 
 * @author CraftedMods
 *
 */
public class PvPCommonCoreUtils
{

    private static Provider provider;

    public static boolean setProvider (Provider provider)
    {
        if (PvPCommonCoreUtils.provider == null)
        {
            PvPCommonCoreUtils.provider = provider;
            return true;
        }
        return false;
    }

    /**
     * Extracts the properties from the {@link Process} annotation of a class
     * annotated with it.
     * 
     * @param clazz
     *            The annotated class
     * @return The properties map
     */
    public static Map<String, String> getPropertiesFromProcessedClass (Class<?> clazz)
    {
        return PvPCommonCoreUtils.getPropertiesFromArray (clazz.getAnnotation (Process.class).properties ());
    }

    /**
     * Extracts the properties from the {@link Register} annotation of a class
     * annotated with it.
     * 
     * @param clazz
     *            The annotated class
     * @return The properties map
     */
    public static Map<String, String> getPropertiesFromRegisteredClass (Class<?> clazz)
    {
        return PvPCommonCoreUtils.getPropertiesFromArray (clazz.getAnnotation (Register.class).properties ());
    }

    /**
     * Returns true of a class has the {@link Register} annotation and
     * {@link Register#enabled()} is true.
     * 
     * @param clazz
     *            The class to check
     * @return Whether it is registerable
     */
    public static <T> boolean isClassRegisterable (Class<? extends T> clazz)
    {
        Register annotation = clazz.getAnnotation (Register.class);
        return annotation != null && annotation.enabled ();
    }

    /**
     * A helper function creating and returning instances of the supplied classes.
     * Additionally, one can control which classes should be instantiated and also a
     * "post create event" can be fired after the creation of a class.
     * 
     * @param classes
     *            The collection of classes
     * @param shouldCreateInstancePredicate
     *            A predicate returning whether a class should be instantiated. If
     *            null, every class will be instantiated.
     * @param postCreateConsumer
     *            If not null, it'll be called for every class and class instance
     *            after the creation of it.
     * @return A collection of all class instances.
     */
    public static <T> Collection<T> createInstances (
        Collection<Class<? extends T>> classes, Predicate<Class<? extends T>> shouldCreateInstancePredicate,
        BiConsumer<Class<? extends T>, T> postCreateConsumer)
    {
        Collection<T> instances = new ArrayList<> ();

        for (Class<? extends T> classToProcess : classes)
        {
            if (shouldCreateInstancePredicate != null ? shouldCreateInstancePredicate.test (classToProcess) : true)
            {

                T newInstance = createInstance (classToProcess);
                if (newInstance != null)
                {
                    instances.add (newInstance);
                    if (postCreateConsumer != null)
                    {
                        postCreateConsumer.accept (classToProcess, newInstance);
                    }
                }
            }
        }

        return instances;
    }

    /**
     * Creates a new instance of the supplied class or returns null of no instance
     * could be created.
     * 
     * @param classToInstantiate
     *            The class to instantiate
     * @return The created instance
     */
    public static <T> T createInstance (Class<T> classToInstantiate)
    {
        try
        {
            return classToInstantiate.newInstance ();
        }
        catch (InstantiationException e)
        {
            PvPMode.proxy.getLogger ().errorThrowable ("Couldn't create an instance of the class \"%s\"", e,
                classToInstantiate.getName ());
        }
        catch (IllegalAccessException e)
        {
            PvPMode.proxy.getLogger ().errorThrowable (
                "Couldn't find or access the default constructor of the class \"%s\"", e,
                classToInstantiate.getName ());
        }
        return null;
    }

    /**
     * Creates instances of the supplied classes as in
     * {@link PvPCommonCoreUtils#createInstances(Collection, Predicate, BiConsumer)},
     * but only if {@link PvPCommonCoreUtils#isClassRegisterable(Class)} returns
     * true for that class.
     * 
     * @param classes
     *            The collection of classes
     * @return A collection of instances of the supplied classes
     */
    public static <T> Collection<T> createRegisteredInstances (Collection<Class<? extends T>> classes)
    {
        return createInstances (classes, PvPCommonCoreUtils::isClassRegisterable, null);
    }

    /**
     * Creates instances of the supplied classes as in
     * {@link PvPCommonCoreUtils#createInstances(Collection, Predicate, BiConsumer)},
     * but only if {@link PvPCommonCoreUtils#isClassRegisterable(Class)} returns
     * true for that class. Also, one can specify a "postCreateConsumer" (or null,
     * then nothing special happens).
     * 
     * @param classes
     *            The collection of classes
     * @param postCreateConsumer
     *            If not null, it'll be called after the class instance has been
     *            created
     * @return A collection of instances of the supplied classes
     */
    public static <T> Collection<T> createRegisteredInstances (Collection<Class<? extends T>> classes,
        BiConsumer<Class<? extends T>, T> postCreateConsumer)
    {
        return createInstances (classes, PvPCommonCoreUtils::isClassRegisterable, postCreateConsumer);
    }

    /**
     * Creates instances of the supplied classes and returns them.
     * 
     * @param classes
     *            The collection of classes
     * @return The created instances
     */
    public static <T> Collection<T> createInstances (
        Collection<Class<? extends T>> classes)
    {
        return createInstances (classes, null, null);
    }

    /**
     * Extracts properties of the format "key=value" from the specified array, where
     * each entry contains a property of the specified format.
     * 
     * @param properties
     *            The properties array
     * @return A map containing the properties
     */
    public static Map<String, String> getPropertiesFromArray (String... properties)
    {
        Map<String, String> propertiesMap = new HashMap<> ();

        for (String property : properties)
        {
            String[] splitProperty = property.split ("=");
            if (splitProperty.length > 1)
            {
                propertiesMap.put (splitProperty[0], splitProperty[1]);
            }
        }

        return propertiesMap;
    }

    /**
     * Returns a {@link SimpleLogger} instance assigned to the specified name.
     * 
     * @param name
     *            The name of the logger
     * @return The logger instance
     */
    public static SimpleLogger getLogger (String name)
    {
        return provider.getLogger (name);
    }

    /**
     * Returns a {@link SimpleLogger} instance assigned to the specified class.
     * 
     * @param clazz
     *            The class the logger is assigned to
     * @return The logger instance
     */
    public static SimpleLogger getLogger (Class<?> clazz)
    {
        return provider.getLogger (clazz.getName ());
    }

    public static interface Provider
    {
        public SimpleLogger getLogger (String name);
    }

}
