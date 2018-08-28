package pvpmode.modules.siegeMode.internal.server;

import pvpmode.api.common.compatibility.ModCompatibilityModuleLoader;

/**
 * The compatibility module loader for the Siege Mode Mod.
 *
 * @author CraftedMods
 *
 */
public class SiegeModeCompatiblityModuleLoader extends ModCompatibilityModuleLoader
{

    public SiegeModeCompatiblityModuleLoader ()
    {
        super ("siegemode");
    }

    @Override
    public String getModuleName ()
    {
        return "Siege Mode Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName ()
    {
        return "pvpmode.modules.siegeMode.internal.server.SiegeModeCompatibilityModule";
    }

}