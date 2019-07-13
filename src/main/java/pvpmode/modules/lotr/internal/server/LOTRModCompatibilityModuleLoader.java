package pvpmode.modules.lotr.internal.server;

import pvpmode.api.common.compatibility.ForgeModCompatibilityModuleLoader;

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
        return "pvpmode.modules.lotr.internal.server.LOTRModCompatibilityModule";
    }

    @Override
    protected boolean isVersionSupported (String modid, String version)
    {
        return version.equals ("Update v35.1 for Minecraft 1.7.10");
    }

}
