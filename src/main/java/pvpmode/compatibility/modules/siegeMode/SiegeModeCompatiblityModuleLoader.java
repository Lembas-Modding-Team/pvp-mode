package pvpmode.compatibility.modules.siegeMode;

import pvpmode.compatibility.ModCompatibilityModuleLoader;

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
        return "pvpmode.compatibility.modules.siegeMode.SiegeModeCompatibilityModule";
    }

}
