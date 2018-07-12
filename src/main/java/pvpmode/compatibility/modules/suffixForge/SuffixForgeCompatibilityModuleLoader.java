package pvpmode.compatibility.modules.suffixForge;

import pvpmode.compatibility.ModCompatibilityModuleLoader;

/**
 * The compatibility module loader for SuffixForge.
 *
 * @author CraftedMods
 *
 */
public class SuffixForgeCompatibilityModuleLoader extends ModCompatibilityModuleLoader
{

    public SuffixForgeCompatibilityModuleLoader ()
    {
        super ("suffixforge");
    }

    @Override
    public String getModuleName ()
    {
        return "Suffix Forge Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName ()
    {
        return "pvpmode.compatibility.modules.suffixForge.SuffixForgeCompatibilityModule";
    }

}