package pvpmode.internal.server.log;

import java.nio.file.Path;
import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import pvpmode.api.server.log.*;

public class CombatLogManagerImpl implements CombatLogManager
{
    private final String defaultHandlerName;

    private Map<String, CombatLogHandler> registeredCombatLogHandlers = new HashMap<> ();

    private Collection<String> activatedHandlerNames = new HashSet<> ();

    private boolean canRegisterHandler = true;
    private boolean canActivateHandler = false;

    public CombatLogManagerImpl (String defaultHandlerName)
    {
        Objects.requireNonNull (defaultHandlerName);

        this.defaultHandlerName = defaultHandlerName;
    }

    @Override
    public String getDefaultHandlerName ()
    {
        return defaultHandlerName;
    }

    @Override
    public void registerCombatLogHandler (String name, CombatLogHandler handler)
    {
        checkState (canRegisterHandler);
        Objects.requireNonNull (name);
        registeredCombatLogHandlers.put (name, handler);
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
    public void activateHandler (String handler)
    {
        checkState (canActivateHandler);
        activatedHandlerNames.add (handler);
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
    public void preInit ()
    {
        checkState (canRegisterHandler);
        canRegisterHandler = false;
        canActivateHandler = true;
    }

    /**
     * Initializes the log manager and loads all activated handlers.</br>
     * You've to call preInit before invoking this function! After calling this
     * function, no new handler can be activated.
     *
     * @param pvpLoggingDir
     *            The directory where logging handlers can store data
     */
    public void init (Path pvpLoggingDir)
    {
        checkState (!canRegisterHandler);
        checkState (canActivateHandler);
        Objects.requireNonNull (pvpLoggingDir);

        for (String handlerName : activatedHandlerNames)
        {
            registeredCombatLogHandlers.get (handlerName).init (pvpLoggingDir);
        }

        canActivateHandler = false;
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
        checkState (!canRegisterHandler);
        checkState (!canActivateHandler);
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
        checkState (!canActivateHandler);
        for (String handlerName : activatedHandlerNames)
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
