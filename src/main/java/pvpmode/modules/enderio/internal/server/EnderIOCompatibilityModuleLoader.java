package pvpmode.modules.enderio.internal.server;

import pvpmode.api.common.compatibility.ForgeModCompatibilityModuleLoader;

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
        return "pvpmode.modules.enderio.internal.server.EnderIOCompatibilityModule";
    }

    @Override
    public String getInternalModuleName ()
    {
        return "enderio";
    }

}
