package pvpmode.compatibility.modules;

import cpw.mods.fml.common.Loader;
import pvpmode.compatibility.CompatibilityModuleLoader;

/**
 * The compatibility module loader for the LOTR Mod.
 * @author CraftedMods
 *
 */
public class LOTRModCompatibilityModuleLoader implements CompatibilityModuleLoader
{
    private Boolean lotrmodLoaded;
    
    @Override
    public String getModuleName ()
    {
        return "LOTR Mod Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName ()
    {
        return "pvpmode.compatibility.modules.LOTRModCompatibilityModule";
    }

    @Override
    public boolean canLoad ()
    {
        if (lotrmodLoaded == null)
            lotrmodLoaded = Loader.isModLoaded ("lotr");
        return lotrmodLoaded.booleanValue ();
    }

}
