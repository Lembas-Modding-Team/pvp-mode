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
     * Registers a combat logging handler instance with an unique name.</br>
     * This can only be done before preInit was called.
     *
     * @param name
     *            The unique handler name
     * @param handler
     *            A handler instance
     */
    public void registerCombatLogHandler (String name, CombatLogHandler handler);

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
     * Adds the specified handler name to the list of activated handlers.</br>
     * This only can be invoked after preInit.
     *
     * @param handler
     *            The handler which should be activated
     */
    public void activateHandler (String handler);

    /**
     * @return An unmodifiable collection containing the names of all activated
     *         handlers
     */
    public Collection<String> getActivatedHandlerNames ();

}