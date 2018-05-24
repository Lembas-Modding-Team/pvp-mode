package pvpmode.log;

import java.nio.file.Path;
import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

/**
 * The combat log manager manages the combat logging modules and their
 * lifecycle. Logging events will be distributed to all active modules.
 * 
 * @author CraftedMods
 *
 */
public class PvPCombatLogManager
{
    private final String defaultHandlerName;

    private Map<String, CombatLogHandler> registeredCombatLogHandlers = new HashMap<String, CombatLogHandler> ();

    private Collection<String> activatedHandlerNames = new HashSet<> ();

    private boolean canRegisterHandler = true;
    private boolean canActivateHandler = false;

    /**
     * Creates a new combat log manager.
     * 
     * @param defaultHandlerName
     *            The default combat log handler
     */
    public PvPCombatLogManager (String defaultHandlerName)
    {
        Objects.requireNonNull (defaultHandlerName);

        this.defaultHandlerName = defaultHandlerName;
    }

    /**
     * @return The name of the default combat logging handler
     */
    public String getDefaultHandlerName ()
    {
        return defaultHandlerName;
    }

    /**
     * Registers a combat logging handler instance with an unique name.</br>
     * This can only be done before preInit was called.
     * 
     * @param name
     *            The unique handler name
     * @param handler
     *            A handler instance
     */
    public void registerCombatLogHandler (String name, CombatLogHandler handler)
    {
        checkState (canRegisterHandler);
        Objects.requireNonNull (name);
        this.registeredCombatLogHandlers.put (name, handler);
    }

    /**
     * @return An array containing the names of all registered handlers
     */
    public String[] getRegisteredHandlerNames ()
    {
        return registeredCombatLogHandlers.keySet ()
            .toArray (new String[registeredCombatLogHandlers.keySet ().size ()]);
    }

    /**
     * Returns whether the specified handler name is the name of a registered
     * handler.
     * 
     * @param name
     *            The handler name which should be tested
     * @return Whether the name is valid
     */
    public boolean isValidHandlerName (String name)
    {
        return registeredCombatLogHandlers.keySet ().contains (name);
    }

    /**
     * Adds the specified handler name to the list of activated handlers.</br>
     * This only can be invoked after preInit.
     * 
     * @param handler
     *            The handler which should be activated
     */
    public void activateHandler (String handler)
    {
        checkState (canActivateHandler);
        this.activatedHandlerNames.add (handler);
    }

    /**
     * @return An unmodifiable collection containing the names of all activated
     *         handlers
     */
    public Collection<String> getActivatedHandlerNames ()
    {
        return Collections.unmodifiableCollection (activatedHandlerNames);
    }

    /**
     * Pre-initializes the log manager. After calling this method, handlers can
     * be activated. From now on, no new handler can be registered.
     */
    public void preInit ()
    {
        checkState (canRegisterHandler);
        this.canRegisterHandler = false;
        this.canActivateHandler = true;
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
            this.registeredCombatLogHandlers.get (handlerName).init (pvpLoggingDir);
        }

        canActivateHandler = false;
    }

    /**
     * This function will be invoked if pvp events occur which should be logged.
     * The manager will distribute the supplied data to all active handlers.
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
            this.registeredCombatLogHandlers.get (handlerName).log (Calendar.getInstance ().getTime (), attacker,
                victim, damageAmount, damageSource);
        }
    }

    /**
     * This function will be invoked if the manager should shutdown. All
     * registered handlers will now cleanup.
     */
    public void close ()
    {
        checkState (!canRegisterHandler);
        checkState (!canActivateHandler);
        for (String handlerName : activatedHandlerNames)
        {
            this.registeredCombatLogHandlers.get (handlerName).cleanup ();
        }
    }

    private void checkState (boolean requiredCondition)
    {
        if (!requiredCondition)
            throw new IllegalStateException ("The PvpCombatLogManager is in a wrong state");
    }
}
