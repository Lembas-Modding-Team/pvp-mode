package pvpmode.api.common.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import org.apache.commons.io.IOUtils;

import net.minecraft.entity.player.EntityPlayer;

public class PvPCommonUtils
{

    public static final String SOMETHING_WENT_WRONG_MESSAGE = "IF YOU SEE THIS, SOMETHING WENT WRONG. PLEASE REPORT IT.";

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
     * Returns a map with the content of the source map, which is completely
     * unmodifiable, inclusive it's content and the content of the content and so
     * on. Changes made to the source map WON'T be backed by the returned map.
     *
     * @param source
     *            The original map
     * @return The deep unmodifiable map
     */
    public static <K, J> Map<K, J> deepUnmodifiableMap (Map<K, J> source)
    {
        Map<K, J> tmpMap = new HashMap<> (source.size ());

        source.forEach ( (key, value) ->
        {
            tmpMap.put (getUnmodifiableObject (key), getUnmodifiableObject (value));
        });
        return Collections.unmodifiableMap (tmpMap);
    }

    /**
     * Returns a list with the content of the source list, which is completely
     * unmodifiable, inclusive it's content and the content of the content and so
     * on. Changes made to the source list WON'T be backed by the returned list.
     *
     * @param source
     *            The original list
     * @return The deep unmodifiable list
     */
    public static <K> List<K> deepUnmodifiableList (List<K> source)
    {
        return deepUnmodifiableCollection (source, new ArrayList<K> (source.size ()), Collections::unmodifiableList);
    }

    /**
     * Returns a set with the content of the source set, which is completely
     * unmodifiable, inclusive it's content and the content of the content and so
     * on. Changes made to the source set WON'T be backed by the returned set.
     *
     * @param source
     *            The original list
     * @return The deep unmodifiable list
     */
    public static <K> Set<K> deepUnmodifiableSet (Set<K> source)
    {
        return deepUnmodifiableCollection (source, new HashSet<K> (source.size ()), Collections::unmodifiableSet);
    }

    /**
     * Returns a collection with the content of the source collection, which is
     * completely unmodifiable, inclusive it's content and the content of the
     * content and so on. Changes made to the source collection WON'T be backed by
     * the returned collection.
     *
     * @param source
     *            The original collection
     * @return The deep unmodifiable collection
     */
    public static <K> Collection<K> deepUnmodifiableCollection (Collection<K> source)
    {
        return deepUnmodifiableCollection (source, new ArrayList<K> (source.size ()),
            Collections::unmodifiableCollection);
    }

    private static <K extends Collection<J>, J> K deepUnmodifiableCollection (K source, K tmpCollection,
        Function<K, K> unmodifiableCollectionCreator)
    {
        source.forEach (element ->
        {
            tmpCollection.add (getUnmodifiableObject (element));
        });
        return unmodifiableCollectionCreator.apply (tmpCollection);
    }

    @SuppressWarnings("unchecked")
    private static <K> K getUnmodifiableObject (K object)
    {
        if (object instanceof List)
            return (K) deepUnmodifiableList ((List<?>) object);
        if (object instanceof Set)
            return (K) deepUnmodifiableSet ((Set<?>) object);
        if (object instanceof Map)
            return (K) deepUnmodifiableMap ((Map<?, ?>) object);
        if (object instanceof Collection)
            return (K) deepUnmodifiableCollection ((Collection<?>) object);
        return object;
    }

}
