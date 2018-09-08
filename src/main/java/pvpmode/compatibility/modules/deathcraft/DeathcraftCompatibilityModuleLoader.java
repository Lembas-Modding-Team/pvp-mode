package pvpmode.compatibility.modules.deathcraft;

import pvpmode.compatibility.BukkitPluginCompatibilityModuleLoader;

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
        return "pvpmode.compatibility.modules.deathcraft.DeathcraftCompatibilityModule";
    }

}
