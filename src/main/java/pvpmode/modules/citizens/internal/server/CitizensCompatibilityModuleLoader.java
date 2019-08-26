package pvpmode.modules.citizens.internal.server;

import cpw.mods.fml.relauncher.Side;
import pvpmode.modules.bukkit.api.server.BukkitPluginCompatibilityModuleLoader;

public class CitizensCompatibilityModuleLoader extends BukkitPluginCompatibilityModuleLoader
{

    public CitizensCompatibilityModuleLoader ()
    {
        super ("Citizens");
    }

    @Override
    public String getModuleName ()
    {
        return "Citizens Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName (Side side)
    {
        return "pvpmode.modules.citizens.internal.server.CitizensCompatibilityModule";
    }

}
