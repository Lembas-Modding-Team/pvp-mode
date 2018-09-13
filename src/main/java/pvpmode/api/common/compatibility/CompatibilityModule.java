package pvpmode.api.common.compatibility;

import java.nio.file.Path;

import pvpmode.api.common.SimpleLogger;

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
     * an exception, the module won't be loaded. The configuration folder isn't
     * required to exist on the filesystem.
     * 
     * @param loader
     *            The loader that loaded this module
     * @param configurationFolder
     *            The configuration folder for the module
     * @param logger
     *            The logger of the module
     * @throws Exception
     *             Thrown by the provider
     */
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception;
}
