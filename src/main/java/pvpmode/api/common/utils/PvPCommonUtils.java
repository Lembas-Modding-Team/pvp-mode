package pvpmode.api.common.utils;

import java.io.*;
import java.nio.file.*;
import java.util.function.Supplier;

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

}
