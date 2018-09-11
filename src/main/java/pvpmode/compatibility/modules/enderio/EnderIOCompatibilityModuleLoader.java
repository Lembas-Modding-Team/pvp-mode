package pvpmode.compatibility.modules.enderio;

import pvpmode.compatibility.ForgeModCompatibilityModuleLoader;

/**
 * The compatibility module loader for Ender IO.
 *
 * @author CraftedMods
 *
 */
public class EnderIOCompatibilityModuleLoader extends ForgeModCompatibilityModuleLoader
{

    public EnderIOCompatibilityModuleLoader ()
    {
        super ("EnderIO");
    }

    @Override
    public String getModuleName ()
    {
        return "Ender IO Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName ()
    {
        return "pvpmode.compatibility.modules.enderio.EnderIOCompatibilityModule";
    }

}
