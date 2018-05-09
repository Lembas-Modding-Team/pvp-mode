package pvpmode.log;

import java.io.IOException;
import java.util.*;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import pvpmode.PvPMode;

public class PvPCombatLog
{
    public static final String NO_HANDLER_NAME = "none";

    private static Map<String, CombatLogHandler> combatLogHandlers = new HashMap<String, CombatLogHandler> ();

    private static final String[] combatLogHandlerNames;

    static
    {
        combatLogHandlers.put (SimpleCombatLogHandler.CONFIG_NAME, new SimpleCombatLogHandler ());
        combatLogHandlers.put (NO_HANDLER_NAME, null);

        combatLogHandlerNames = combatLogHandlers.keySet ().toArray (new String[combatLogHandlers.keySet ().size ()]);
    }

    public static String getDefaultHandlerName()
    {
        return SimpleCombatLogHandler.CONFIG_NAME;
    }

    public static String[] getValidHandlerNames()
    {
        return combatLogHandlerNames;
    }

    public static boolean isValidHandlerName(String name)
    {
        return combatLogHandlers.keySet ().contains (name);
    }

    public static void init(FMLPreInitializationEvent event) throws IOException
    {
        for (CombatLogHandler handler : combatLogHandlers.values ())
        {
            if (handler != null)
            {
                handler.init (event);
            }
        }
    }

    public static void log(EntityPlayer attacker, EntityPlayer victim, float damageAmount, DamageSource damageSource)
    {
        CombatLogHandler handler = combatLogHandlers.get (PvPMode.pvpLoggingHandler);
        if (handler != null)
        {
            handler.log (Calendar.getInstance ().getTime (), attacker, victim, damageAmount, damageSource);
        }
    }

    public static void close()
    {
        for (CombatLogHandler handler : combatLogHandlers.values ())
        {
            if (handler != null)
            {
                handler.cleanup ();
            }
        }
    }
}
