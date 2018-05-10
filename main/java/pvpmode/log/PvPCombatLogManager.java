package pvpmode.log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import pvpmode.PvPMode;

public class PvPCombatLogManager
{
    public static final String NO_HANDLER_NAME = "none";

    private final String defaultHandlerName;

    private Map<String, CombatLogHandler> combatLogHandlers = new HashMap<String, CombatLogHandler> ();

    public PvPCombatLogManager(String defaultHandlerName)
    {
        Objects.requireNonNull (defaultHandlerName);

        this.defaultHandlerName = defaultHandlerName;

        combatLogHandlers.put (NO_HANDLER_NAME, null);
    }

    public String getDefaultHandlerName()
    {
        return defaultHandlerName;
    }

    public void registerCombatLogHandler(String name, CombatLogHandler handler)
    {
        Objects.requireNonNull (name);

        this.combatLogHandlers.put (name, handler);

    }

    public String[] getRegisteredHandlerNames()
    {
        return combatLogHandlers.keySet ()
                        .toArray (new String[combatLogHandlers.keySet ().size ()]);
    }

    public boolean isValidHandlerName(String name)
    {
        return combatLogHandlers.keySet ().contains (name);
    }

    public void init(Path pvpLoggingDir) throws IOException
    {
        Objects.requireNonNull (pvpLoggingDir);

        for (CombatLogHandler handler : combatLogHandlers.values ())
        {
            if (handler != null)
            {
                handler.init (pvpLoggingDir);
            }
        }
    }

    public void log(EntityPlayer attacker, EntityPlayer victim, float damageAmount, DamageSource damageSource)
    {
        Objects.requireNonNull (attacker);
        Objects.requireNonNull (victim);
        Objects.requireNonNull (damageSource);

        CombatLogHandler handler = combatLogHandlers.get (PvPMode.pvpLoggingHandler);
        if (handler != null)
        {
            handler.log (Calendar.getInstance ().getTime (), attacker, victim, damageAmount, damageSource);
        }
    }

    public void close()
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
