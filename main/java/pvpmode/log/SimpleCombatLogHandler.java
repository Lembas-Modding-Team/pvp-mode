package pvpmode.log;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class SimpleCombatLogHandler implements CombatLogHandler
{
    public static final String CONFIG_NAME = "default";

    private PrintWriter stream;

    @Override
    public void init(Path pvpLoggingDir)
    {
        try
        {
            File logFile = new File (pvpLoggingDir.getParent ().toFile (), "pvpmode.log");

            if (!logFile.exists ())
            {
                logFile.getParentFile ().mkdirs ();
                logFile.createNewFile ();
            }

            stream = new PrintWriter (logFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException ("The pvp log file couldn't be created or accessed", e);
        }

    }

    @Override
    public void log(Date date, EntityPlayer attacker, EntityPlayer victim, float damageAmount,
                    DamageSource damageSource)
    {
        stream.println (String.format (
                        "[%s] %s or an unit of this player initiated an attack against %s dealing %.2f HP damage of the type %s",
                        SimpleDateFormat.getDateTimeInstance ().format (date), attacker.getDisplayName (),
                        victim.getDisplayName (), damageAmount, damageSource.damageType));
        stream.flush ();

    }

    @Override
    public void cleanup()
    {
        stream.close ();

    }

}
