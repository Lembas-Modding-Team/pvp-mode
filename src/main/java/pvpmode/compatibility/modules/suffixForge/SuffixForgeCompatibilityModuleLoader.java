package pvpmode.compatibility.modules.suffixForge;

import cpw.mods.fml.common.Loader;
import pvpmode.compatibility.CompatibilityModuleLoader;

/**
 * The compatibility module loader for SuffixForge.
 * 
 * @author CraftedMods
 *
 */
public class SuffixForgeCompatibilityModuleLoader implements CompatibilityModuleLoader
{
    private Boolean suffixForgeLoaded;

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

    @Override
    public boolean canLoad ()
    {
        if (suffixForgeLoaded == null)
            suffixForgeLoaded = Loader.isModLoaded ("suffixforge");
        return suffixForgeLoaded.booleanValue ();
    }

}