package pvpmode.modules.siegeMode.internal.server;

import cpw.mods.fml.relauncher.Side;
import pvpmode.api.common.compatibility.ForgeModCompatibilityModuleLoader;

/**
 * The compatibility module loader for the Siege Mode Mod.
 *
 * @author CraftedMods
 *
 */
public class SiegeModeCompatibilityModuleLoader extends ForgeModCompatibilityModuleLoader
{

    public SiegeModeCompatibilityModuleLoader ()
    {
        super ("siegemode");
    }

    @Override
    public String getModuleName ()
    {
        return "Siege Mode Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName (Side side)
    {
        return "pvpmode.modules.siegeMode.internal.server.SiegeModeCompatibilityModule";
    }
    
    @Override
    protected boolean isVersionSupported (String modid, String version)
    {
        return version.equals ("1.0");
    }

}
