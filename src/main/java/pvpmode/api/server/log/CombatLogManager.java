package pvpmode.api.server.log;

import java.util.Collection;

/**
 * The combat log manager manages the combat logging modules and their
 * lifecycle. Logging events will be distributed to all active modules.
 *
 * @author CraftedMods
 *
 */
public interface CombatLogManager
{

    /**
     * @return The name of the default combat logging handler
     */
    public String getDefaultHandlerName ();

    /**
     * Registers a combat logging handler instance with an unique name. Returns
     * false if the handler couldn't be registered. If the handler is already
     * registered, the new instance will override the current one.
     *
     * @param name
     *            The unique handler name
     * @param handler
     *            A handler instance
     * @return Whether the handler could be registered
     */
    public boolean registerCombatLogHandler (String name, CombatLogHandler handler);

    /**
     * @return An array containing the names of all registered handlers
     */
    public String[] getRegisteredHandlerNames ();

    /**
     * Returns whether the specified handler name is the name of a registered
     * handler.
     *
     * @param name
     *            The handler name which should be tested
     * @return Whether the name is valid
     */
    public boolean isValidHandlerName (String name);

    /**
     * Activates the specified handler and returns true if it was activated. Returns
     * false if the hander couldn't be activated or if it isn't a registered one.
     *
     * @param handler
     *            The handler which should be activated
     * @return Whether the handler could be activated
     */
    public boolean activateHandler (String handler);

    /**
     * Deactivates the specified handler and returns true if it was deactivated.
     * Returns false if the hander couldn't be deactivated or if it isn't a registered
     * or activated one.
     *
     * @param handler
     *            The handler which should be deactivated
     * @return Whether the handler could be deactivated
     */
    public boolean deactivateHandler (String handler);

    /**
     * @return An unmodifiable collection containing the names of all activated
     *         handlers
     */
    public Collection<String> getActivatedHandlerNames ();

}
