package pvpmode.log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class PvPCombatLogManager
{
    private final String defaultHandlerName;

    private Map<String, CombatLogHandler> registeredCombatLogHandlers = new HashMap<String, CombatLogHandler> ();

    private Collection<String> activatedHandlerNames = new HashSet<> ();

    private boolean canRegisterHandler = true;

    public PvPCombatLogManager(String defaultHandlerName)
    {
        Objects.requireNonNull (defaultHandlerName);

        this.defaultHandlerName = defaultHandlerName;
    }

    public String getDefaultHandlerName()
    {
        return defaultHandlerName;
    }

    public void registerCombatLogHandler(String name, CombatLogHandler handler)
    {
        checkState (canRegisterHandler);
        Objects.requireNonNull (name);
        this.registeredCombatLogHandlers.put (name, handler);
    }

    public String[] getRegisteredHandlerNames()
    {
        return registeredCombatLogHandlers.keySet ()
                        .toArray (new String[registeredCombatLogHandlers.keySet ().size ()]);
    }

    public boolean isValidHandlerName(String name)
    {
        return registeredCombatLogHandlers.keySet ().contains (name);
    }

    public void activateHandler(String handler)
    {
        checkState (!canRegisterHandler);
        this.activatedHandlerNames.add (handler);
    }

    public Collection<String> getActivatedHandlerNames()
    {
        return Collections.unmodifiableCollection (activatedHandlerNames);
    }

    public void preInit()
    {
        checkState (canRegisterHandler);
        this.canRegisterHandler = false;
    }

    public void init(Path pvpLoggingDir) throws IOException
    {
        checkState (!canRegisterHandler);
        Objects.requireNonNull (pvpLoggingDir);

        for (String handlerName : activatedHandlerNames)
        {
            this.registeredCombatLogHandlers.get (handlerName).init (pvpLoggingDir);
        }
    }

    public void log(EntityPlayer attacker, EntityPlayer victim, float damageAmount, DamageSource damageSource)
    {
        checkState (!canRegisterHandler);
        Objects.requireNonNull (attacker);
        Objects.requireNonNull (victim);
        Objects.requireNonNull (damageSource);

        for (String handlerName : activatedHandlerNames)
        {
            this.registeredCombatLogHandlers.get (handlerName).log (Calendar.getInstance ().getTime (), attacker,
                            victim, damageAmount, damageSource);
        }
    }

    public void close()
    {
        checkState (!canRegisterHandler);
        for (String handlerName : activatedHandlerNames)
        {
            this.registeredCombatLogHandlers.get (handlerName).cleanup ();
        }
    }

    private void checkState(boolean requiredCondition)
    {
        if (!requiredCondition)
            throw new IllegalStateException ("The PvpCombatLogManager is in a wrong state");
    }
}
