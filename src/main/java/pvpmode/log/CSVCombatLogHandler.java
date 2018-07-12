package pvpmode.log;

import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import pvpmode.PvPMode;

/**
 * A combat log handler which logs the PvP event data to a file in the CSV
 * format - this represents the data as a table.
 *
 * @author CraftedMods
 *
 */
public class CSVCombatLogHandler extends AbstractFileCombatLogHandler
{

    public static final String CONFIG_NAME = "csv";
    public static final String DEFAULT_CSV_SEPARATOR = ";";

    public CSVCombatLogHandler ()
    {
        super (CONFIG_NAME, "csv");
    }

    @Override
    public void init (Path pvpLoggingDir)
    {
        super.init (pvpLoggingDir);
        writer.println (String.format ("Date/Time:%sAttacker:%<sVictim:%<sDamage Amount:%<sDamage Source:",
            PvPMode.csvSeparator));// CSV header
    }

    @Override
    public void log (Date date, EntityPlayer attacker, EntityPlayer victim, float damageAmount,
        DamageSource damageSource)
    {
        writer.println (String.format ("%2$s%1$s%3$s%1$s%4$s%1$s%5$.2f%1$s%6$s", PvPMode.csvSeparator,
            DateFormat.getDateTimeInstance ().format (date), attacker.getDisplayName (),
            victim.getDisplayName (), damageAmount, damageSource.damageType));

    }

}
