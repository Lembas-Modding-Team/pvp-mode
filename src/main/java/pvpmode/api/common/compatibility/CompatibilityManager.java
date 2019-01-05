package pvpmode.api.common.compatibility;

import java.util.Map;

/**
 * The compatibility manager manages and loads the compatibility modules.
 * PvPMode and other mods will register their compatibility modules via this
 * manager.
 *
 * @author CraftedMods
 *
 */
public interface CompatibilityManager
{

    /**
     * Registers the supplied compatibility module loader.
     *
     * @param moduleLoader
     *            The loader to register
     * @return Whether the loader could be registered
     */
    public boolean registerModuleLoader (Class<? extends CompatibilityModuleLoader> moduleLoader);

    /**
     * Unregisters a previously registered compatibility module loader. If the
     * compatibility module of the supplied loader was already loaded, false will be
     * returned.
     *
     * @param moduleLoader
     *            The loader to unregister
     * @return Whether the loaded could be unregistered
     */
    public boolean unregisterModuleLoader (Class<? extends CompatibilityModuleLoader> moduleLoader);

    /**
     * Returns a map containing the compatibility module loader as key and the
     * compatibility module as value for all currently loaded compatibility modules.
     * If no registered modules were loaded, an empty map will be returned.
     * 
     * @return The loaded compatibility module with their loaders
     */
    public Map<CompatibilityModuleLoader, CompatibilityModule> getLoadedModules ();

}