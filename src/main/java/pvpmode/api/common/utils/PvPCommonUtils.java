package pvpmode.api.common.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import net.minecraft.entity.player.EntityPlayer;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;

public class PvPCommonUtils
{

    private static Provider provider;

    public static boolean setProvider (Provider provider)
    {
        if (PvPCommonUtils.provider == null)
        {
            PvPCommonUtils.provider = provider;
            return true;
        }
        return false;
    }

    public static final String SOMETHING_WENT_WRONG_MESSAGE = "IF YOU SEE THIS, SOMETHING WENT WRONG. PLEASE REPORT IT.";

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPERS_MAP = new HashMap<> ();
    private static final Map<Class<?>, Class<?>> WRAPPERS_TO_PRIMITIVE_MAP = new HashMap<> ();

    static
    {
        PRIMITIVE_TO_WRAPPERS_MAP.put (boolean.class, Boolean.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put (byte.class, Byte.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put (short.class, Short.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put (int.class, Integer.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put (long.class, Long.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put (float.class, Float.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put (double.class, Double.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put (char.class, Character.class);

        // Swap the entries in the PRIMITIVE_TO_WRAPPERS_MAP map
        WRAPPERS_TO_PRIMITIVE_MAP
            .putAll (PRIMITIVE_TO_WRAPPERS_MAP.entrySet ().stream ()
                .collect (Collectors.toMap (Map.Entry::getValue, Map.Entry::getKey)));
    }

    /**
     * Writes the contents of the supplied stream to the specified file.<br/>
     * The file must exist on the filesystem.
     *
     * @param stream
     *            A supplier which creates the input stream
     * @param file
     *            The file where the data should be stored
     * @throws IOException
     *             If IO errors occur
     */
    public static void writeFromStreamToFile (Supplier<InputStream> stream, Path file) throws IOException
    {
        try (InputStream in = stream.get ();
            OutputStream out = Files.newOutputStream (file))
        {
            IOUtils.copy (in, out);
        }
    }

    /**
     * Returns "enabled" if the supplied boolean is true, "disabled" otherwise.
     */
    public static String getEnabledString (boolean enabled)
    {
        return enabled ? "enabled" : "disabled";
    }

    /**
     * Returns the direction of the supplied player relative to the other supplied
     * player.
     */
    public static String getPlayerDirection (EntityPlayer origin, EntityPlayer player)
    {
        double toPlayerX = player.posX - origin.posX;
        double toPlayerZ = player.posZ - origin.posZ;

        double angle = -90 - Math
            .toDegrees (Math.atan2 (toPlayerZ, toPlayerX));

        if (angle < 0)
        {
            angle += 360;
        }

        String direction = SOMETHING_WENT_WRONG_MESSAGE;

        if (angle >= 0.0 && angle <= 22.5 || angle >= 337.5 && angle <= 360.0)
        {
            direction = "N";
        }
        else if (angle > 22.5 && angle < 67.5)
        {
            direction = "NW";
        }
        else if (angle >= 67.5 && angle <= 112.5)
        {
            direction = "W";
        }
        else if (angle > 112.5 && angle < 157.5)
        {
            direction = "SW";
        }
        else if (angle >= 157.5 && angle <= 202.5)
        {
            direction = "S";
        }
        else if (angle > 202.5 && angle < 247.5)
        {
            direction = "SE";
        }
        else if (angle >= 247.5 && angle <= 292.5)
        {
            direction = "E";
        }
        else if (angle > 292.5 && angle < 337.5)
        {
            direction = "NE";
        }
        return direction;
    }

    /**
     * Returns the wrapper class of the specified primitive class. If the supplied
     * class is not a primitive one, the supplied class is returned.
     * 
     * @param primitive
     *            The primitive class
     * @return The wrapper class
     */
    public static Class<?> toWrapper (Class<?> primitive)
    {
        return PRIMITIVE_TO_WRAPPERS_MAP.getOrDefault (primitive, primitive);
    }

    /**
     * Returns the primitive class of the specified wrapper class. If the supplied
     * class is not a wrapper, the supplied class is returned.
     * 
     * @param wrapper
     *            The wrapper class
     * @return The primitive class
     */
    public static Class<?> toPrimitive (Class<?> wrapper)
    {
        return WRAPPERS_TO_PRIMITIVE_MAP.getOrDefault (wrapper, wrapper);
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
     * Extracts the properties from the {@link Process} annotation of a class
     * annotated with it.
     * 
     * @param clazz
     *            The annotated class
     * @return The properties map
     */
    public static Map<String, String> getPropertiesFromProcessedClass (Class<?> clazz)
    {
        return PvPCommonUtils.getPropertiesFromArray (clazz.getAnnotation (Process.class).properties ());
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
        return PvPCommonUtils.getPropertiesFromArray (clazz.getAnnotation (Register.class).properties ());
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
     * "post create event" can be fired after the creation of a class. If error
     * occur while the creation of the class, they'll be logged.
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

                try
                {
                    T newInstance = classToProcess.newInstance ();
                    instances.add (newInstance);
                    if (postCreateConsumer != null)
                    {
                        postCreateConsumer.accept (classToProcess, newInstance);
                    }
                }
                catch (Exception e)
                {
                    PvPMode.proxy.getLogger ().errorThrowable ("Couldn't create a new instance of the class \"%s\"", e,
                        classToProcess.getName ());
                }
            }
        }

        return instances;
    }

    /**
     * Creates instances of the supplied classes as in
     * {@link PvPCommonUtils#createInstances(Collection, Predicate, BiConsumer)},
     * but only if {@link PvPCommonUtils#isClassRegisterable(Class)} returns true
     * for that class.
     * 
     * @param classes
     *            The collection of classes
     * @return A collection of instances of the supplied classes
     */
    public static <T> Collection<T> createRegisteredInstances (Collection<Class<? extends T>> classes)
    {
        return createInstances (classes, PvPCommonUtils::isClassRegisterable, null);
    }

    /**
     * Creates instances of the supplied classes as in
     * {@link PvPCommonUtils#createInstances(Collection, Predicate, BiConsumer)},
     * but only if {@link PvPCommonUtils#isClassRegisterable(Class)} returns true
     * for that class. Also, one can specify a "postCreateConsumer" (or null, then
     * nothing special happens).
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
        return createInstances (classes, PvPCommonUtils::isClassRegisterable, postCreateConsumer);
    }

    /**
     * Creates instances of the supplied classes and returns them. Errors occuring
     * while the creation of the classes will be logged.
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
