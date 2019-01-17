package pvpmode.api.common.compatibility;

import cpw.mods.fml.common.Loader;

/**
 * A compatibility module loader which determines whether a compatibility module
 * can be loaded by the presence of a MinecraftForge mod.
 *
 * @author CraftedMods
 *
 */
public abstract class ForgeModCompatibilityModuleLoader extends IdentifierCompatibilityModuleLoader
{

    protected ForgeModCompatibilityModuleLoader (String... modids)
    {
        super (modids);
    }

    @Override
    public boolean canLoad ()
    {
        return super.canLoad ();
    }

    @Override
    protected boolean isDependencyLoaded (String identifier)
    {
        return Loader.isModLoaded (identifier);
    }

    @Override
    protected String getIdentifierVersion (String identifier)
    {
        return Loader.instance ().getIndexedModList ().get (identifier).getVersion ();
    }
}
