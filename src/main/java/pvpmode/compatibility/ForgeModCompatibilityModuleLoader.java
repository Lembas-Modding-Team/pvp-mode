package pvpmode.compatibility;

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
    protected boolean isDependencyLoaded (String identifier)
    {
        return Loader.isModLoaded (identifier);
    }
}
