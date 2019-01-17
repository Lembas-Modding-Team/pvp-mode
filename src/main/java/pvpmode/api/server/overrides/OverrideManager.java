package pvpmode.api.server.overrides;

/**
 * The override manager manages the conditional PvP mode overrides. Custom
 * override conditions can be registered here.
 *
 * @author CraftedMods
 *
 */
public interface OverrideManager
{

    /**
     * Registers a new PvP override condition.<br/>
     * Override conditions can be registered everytime.
     *
     * @param condition
     *            The condition to register
     * @return Whether the condition could be registered
     */
    public boolean registerOverrideCondition (PvPOverrideCondition condition);

    /**
     * Unregisters a new PvP override condition.<br/>
     * Override conditions can be unregistered everytime.
     *
     * @param condition
     *            The condition to unregister
     * @return Whether the condition could be unregistered
     */
    public boolean unregisterOverrideCondition (PvPOverrideCondition condition);

}