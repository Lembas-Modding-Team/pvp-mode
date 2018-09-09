package pvpmode.compatibility.modules.lotr;

import pvpmode.compatibility.ForgeModCompatibilityModuleLoader;

/**
 * The compatibility module loader for the LOTR Mod.
 *
 * @author CraftedMods
 *
 */
public class LOTRModCompatibilityModuleLoader extends ForgeModCompatibilityModuleLoader
{

    public LOTRModCompatibilityModuleLoader ()
    {
        super ("lotr");
    }

    @Override
    public String getModuleName ()
    {
        return "LOTR Mod Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName ()
    {
        return "pvpmode.compatibility.modules.lotr.LOTRModCompatibilityModule";
    }

}
