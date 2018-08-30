package pvpmode.api.common.compatibility;

import cpw.mods.fml.common.Loader;

/**
 * A compatibility module loader which determines whether a compatibility module
 * can be loaded by the presence of a mod.
 *
 * @author CraftedMods
 *
 */
public abstract class ModCompatibilityModuleLoader implements CompatibilityModuleLoader
{

    private final String modid;
    private Boolean modLoaded;

    protected ModCompatibilityModuleLoader (String modid)
    {
        this.modid = modid;
    }

    public String getModid ()
    {
        return modid;
    }

    @Override
    public String getInternalModuleName ()
    {
        return modid;
    }

    @Override
    public boolean canLoad ()
    {
        if (modLoaded == null)
        {
            modLoaded = Loader.isModLoaded (modid);
        }
        return modLoaded.booleanValue ();
    }

}
