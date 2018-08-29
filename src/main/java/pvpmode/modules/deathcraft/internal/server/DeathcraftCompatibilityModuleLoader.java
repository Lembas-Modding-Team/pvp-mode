package pvpmode.modules.deathcraft.internal.server;

import pvpmode.api.common.compatibility.BukkitPluginCompatibilityModuleLoader;

public class DeathcraftCompatibilityModuleLoader extends BukkitPluginCompatibilityModuleLoader
{

    public DeathcraftCompatibilityModuleLoader ()
    {
        super ("deathcraft");
    }

    @Override
    public String getModuleName ()
    {
        return "Deathcraft Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName ()
    {
        return "pvpmode.modules.deathcraft.internal.server.DeathcraftCompatibilityModule";
    }

}
