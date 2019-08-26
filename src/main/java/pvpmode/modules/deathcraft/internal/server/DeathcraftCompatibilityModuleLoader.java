package pvpmode.modules.deathcraft.internal.server;

import cpw.mods.fml.relauncher.Side;
import pvpmode.modules.bukkit.api.server.BukkitPluginCompatibilityModuleLoader;

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
    public String getCompatibilityModuleClassName (Side side)
    {
        return "pvpmode.modules.deathcraft.internal.server.DeathcraftCompatibilityModule";
    }

    @Override
    protected boolean isVersionSupported (String identifier, String version)
    {
        return version.equals ("1.12a");
    }

}
