package pvpmode.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

/**
 * A combat log handler which simply logs the pvp events to a file.
 * 
 * @author CraftedMods
 *
 */
public class SimpleCombatLogHandler extends AbstractFileCombatLogHandler
{
    public static final String CONFIG_NAME = "default";

    public SimpleCombatLogHandler()
    {
        super (CONFIG_NAME, "log");
    }

    @Override
    public void log(Date date, EntityPlayer attacker, EntityPlayer victim, float damageAmount,
                    DamageSource damageSource)
    {
        writer.println (String.format (
                        "[%s] %s or an unit of this player initiated an attack against %s dealing %.2f HP damage of the type %s",
                        SimpleDateFormat.getDateTimeInstance ().format (date), attacker.getDisplayName (),
                        victim.getDisplayName (), damageAmount, damageSource.damageType));

    }

}
