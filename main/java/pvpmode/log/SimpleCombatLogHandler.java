package pvpmode.log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class SimpleCombatLogHandler implements CombatLogHandler
{
    public static final String CONFIG_NAME = "default";

    private PrintWriter stream;

    @Override
    public void init(FMLPreInitializationEvent event)
    {
        try
        {
            File mcDirectory = event.getModConfigurationDirectory ().getParentFile ();
            File logDirectory = new File (mcDirectory, "logs");
            File logFile = new File (logDirectory, "pvpmode.log");

            if (!logFile.exists ())
                logFile.createNewFile ();

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
