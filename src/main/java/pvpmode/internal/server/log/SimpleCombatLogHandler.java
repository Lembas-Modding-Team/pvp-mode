package pvpmode.internal.server.log;

import java.text.DateFormat;
import java.util.Date;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import pvpmode.api.server.log.*;

/**
 * A combat log handler which simply logs the PvP events to a file.
 *
 * @author CraftedMods
 *
 */
public class SimpleCombatLogHandler extends AbstractFileCombatLogHandler
{
    public SimpleCombatLogHandler ()
    {
        super (LogHandlerConstants.SIMPLE_CONFIG_NAME, "log");
    }

    @Override
    public void log (Date date, EntityPlayer attacker, EntityPlayer victim, float damageAmount,
        DamageSource damageSource)
    {
        writer.println (String.format (
            "[%s] %s or a (hired) unit of this player initiated an attack against %s dealing %.2f HP damage of the type %s",
            DateFormat.getDateTimeInstance ().format (date), attacker.getDisplayName (),
            victim.getDisplayName (), damageAmount, damageSource.damageType));

    }

}
