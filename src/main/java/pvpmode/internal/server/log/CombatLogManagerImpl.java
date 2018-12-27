package pvpmode.internal.server.log;

import java.nio.file.Path;
import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import pvpmode.api.server.log.*;

public class CombatLogManagerImpl implements CombatLogManager
{
    private final String defaultHandlerName;
    private final Path pvpLoggingDirectory;

    private Map<String, CombatLogHandler> registeredCombatLogHandlers = new HashMap<> ();

    private Collection<String> initializedHandlerNames = new HashSet<> ();
    private Collection<String> activatedHandlerNames = new HashSet<> ();

    private boolean canRegisterHandler = true;

    public CombatLogManagerImpl (String defaultHandlerName, Path pvpLoggingDirectory)
    {
        Objects.requireNonNull (defaultHandlerName);

        this.defaultHandlerName = defaultHandlerName;
        this.pvpLoggingDirectory = pvpLoggingDirectory;
    }

    @Override
    public String getDefaultHandlerName ()
    {
        return defaultHandlerName;
    }

    @Override
    public boolean registerCombatLogHandler (String name, CombatLogHandler handler)
    {
        if (canRegisterHandler)
        {
            Objects.requireNonNull (name);
            registeredCombatLogHandlers.put (name, handler);
            return true;
        }
        return false;
    }

    @Override
    public String[] getRegisteredHandlerNames ()
    {
        return registeredCombatLogHandlers.keySet ()
            .toArray (new String[registeredCombatLogHandlers.keySet ().size ()]);
    }

    @Override
    public boolean isValidHandlerName (String name)
    {
        return registeredCombatLogHandlers.keySet ().contains (name);
    }

    @Override
    public boolean activateHandler (String handler)
    {
        if (!canRegisterHandler && registeredCombatLogHandlers.containsKey (handler))
        {
            activatedHandlerNames.add (handler);

            if (!initializedHandlerNames.contains (handler))
            {
                initializedHandlerNames.add (handler);
                registeredCombatLogHandlers.get (handler).init (pvpLoggingDirectory);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivateHandler (String handler)
    {
        if (!canRegisterHandler && activatedHandlerNames.contains (handler))
        {
            activatedHandlerNames.remove (handler);
            return true;
        }
        return false;
    }

    @Override
    public Collection<String> getActivatedHandlerNames ()
    {
        return Collections.unmodifiableCollection (activatedHandlerNames);
    }

    /**
     * Pre-initializes the log manager. After calling this method, handlers can be
     * activated. From now on, no new handler can be registered.
     */
    public void init ()
    {
        checkState (canRegisterHandler);
        canRegisterHandler = false;
    }

    /**
     * This function will be invoked if pvp events occur which should be logged. The
     * manager will distribute the supplied data to all active handlers.
     *
     * @param attacker
     *            The attacking player
     * @param victim
     *            The attacked player
     * @param damageAmount
     *            The amount of damage which was dealt
     * @param damageSource
     *            The damage source
     */
    public void log (EntityPlayer attacker, EntityPlayer victim, float damageAmount, DamageSource damageSource)
    {
        this.checkState (!canRegisterHandler);

        Objects.requireNonNull (attacker);
        Objects.requireNonNull (victim);
        Objects.requireNonNull (damageSource);

        for (String handlerName : activatedHandlerNames)
        {
            registeredCombatLogHandlers.get (handlerName).log (Calendar.getInstance ().getTime (), attacker,
                victim, damageAmount, damageSource);
        }
    }

    /**
     * This function will be invoked if the manager should shutdown. All registered
     * handlers will now cleanup.
     */
    public void close ()
    {
        checkState (!canRegisterHandler);

        for (String handlerName : initializedHandlerNames)
        {
            registeredCombatLogHandlers.get (handlerName).cleanup ();
        }
    }

    private void checkState (boolean requiredCondition)
    {
        if (!requiredCondition)
            throw new IllegalStateException ("The PvPCombatLogManager is in a wrong state");
    }
}
