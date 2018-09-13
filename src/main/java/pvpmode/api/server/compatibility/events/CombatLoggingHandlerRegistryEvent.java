package pvpmode.api.server.compatibility.events;

import java.util.*;

import cpw.mods.fml.common.eventhandler.Event;
import pvpmode.api.server.log.CombatLogHandler;

/**
 * An event for registering combat log handlers. It'll be fired if the combat
 * log manager gets instantiated.
 * 
 * @author CraftedMods
 *
 */
public class CombatLoggingHandlerRegistryEvent extends Event
{

    private Map<String, CombatLogHandler> registeredHandlers = new HashMap<> ();

    /**
     * Registers a handler with the specified name.
     * 
     * @param name
     *            The handler name
     * @param handler
     *            The handler to register
     */
    public void registerHandler (String name, CombatLogHandler handler)
    {
        registeredHandlers.put (name, handler);
    }

    /**
     * Returns all currently registered handlers with the registered name.
     * 
     * @return A map of all registered handlers
     */
    public Map<String, CombatLogHandler> getRegisteredHandlers ()
    {
        return Collections.unmodifiableMap (registeredHandlers);
    }

}
