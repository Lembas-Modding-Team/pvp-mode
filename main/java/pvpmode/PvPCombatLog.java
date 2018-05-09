package pvpmode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class PvPCombatLog
{
    static PrintWriter stream;

    public static void init(FMLPreInitializationEvent event) throws IOException
    {
        File mcDirectory = event.getModConfigurationDirectory ().getParentFile ();
        File logDirectory = new File (mcDirectory, "logs");
        File logFile = new File (logDirectory, "pvpmode.log");

        if (!logFile.exists ())
            logFile.createNewFile ();

        stream = new PrintWriter (logFile);
    }

    public static void log(String message)
    {
        SimpleDateFormat df = new SimpleDateFormat ("HH:mm:ss: ");
        stream.println (df.format (new Date ()) + message);
        stream.flush ();
    }

    public static void close()
    {
        stream.close ();
    }
}
