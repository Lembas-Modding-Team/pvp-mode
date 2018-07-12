package pvpmode.compatibility;

/**
 * A compatibility module interfaces with PvPMode and other mods and provides
 * compatibility with these. The module can contain compile-time dependencies
 * which don't have to be met in runtime - the module then simply won't be
 * loaded. Also, one can subscribe to the compatibility events of PvPMode -
 * they'll be fired into the Forge event bus.
 *
 * @author CraftedMods
 *
 */
public interface CompatibilityModule
{
    /**
     * This function will be executed once when the module is loaded. If it throws
     * an exception, the module won't be loaded.
     */
    public void load () throws Exception;
}
