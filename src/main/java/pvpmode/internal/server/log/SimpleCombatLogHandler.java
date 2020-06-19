package pvpmode.internal.server.log;

import java.text.DateFormat;
import java.util.*;

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
    public void log (Date date, UUID attackerUUID, UUID victimUUID, float damageAmount,
        DamageSource damageSource)
    {
        writer.println (String.format (
            "[%s] %s or a (hired) unit of them initiated an attack against %s (or a (hired) unit of them) dealing %.2f HP damage of the type %s",
            DateFormat.getDateTimeInstance ().format (date), getUsername (attackerUUID),
            getUsername (victimUUID), damageAmount, damageSource.damageType));

    }

}
