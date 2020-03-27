package pvpmode.internal.server.log;

import java.nio.file.Path;
import java.text.DateFormat;
import java.util.*;

import net.minecraft.util.DamageSource;
import pvpmode.PvPMode;
import pvpmode.api.server.log.*;

/**
 * A combat log handler which logs the PvP event data to a file in the CSV
 * format - this represents the data as a table.
 *
 * @author CraftedMods
 *
 */
public class CSVCombatLogHandler extends AbstractFileCombatLogHandler
{

    public CSVCombatLogHandler ()
    {
        super (LogHandlerConstants.CSV_CONFIG_NAME, "csv");
    }

    @Override
    public void init (Path pvpLoggingDir)
    {
        super.init (pvpLoggingDir);
        writer.println (String.format ("Date/Time:%sAttacker:%<sVictim:%<sDamage Amount:%<sDamage Source:",
            getCSVSeparator ()));// CSV header
    }

    @Override
    public void log (Date date, UUID attackerUUID, UUID victimUUID, float damageAmount,
        DamageSource damageSource)
    {
        writer.println (String.format ("%2$s%1$s%3$s%1$s%4$s%1$s%5$.2f%1$s%6$s",
            getCSVSeparator (),
            DateFormat.getDateTimeInstance ().format (date), getUsername (attackerUUID),
            getUsername (victimUUID), damageAmount, damageSource.damageType));

    }

    public String getCSVSeparator ()
    {
        return PvPMode.instance.getServerProxy ().getConfiguration ().getCSVSeparator ();
    }

}
