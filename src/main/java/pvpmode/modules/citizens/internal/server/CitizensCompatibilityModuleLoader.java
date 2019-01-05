package pvpmode.modules.citizens.internal.server;

import pvpmode.api.server.compatibility.BukkitPluginCompatibilityModuleLoader;

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
    public String getCompatibilityModuleClassName ()
    {
        return "pvpmode.modules.citizens.internal.server.CitizensCompatibilityModule";
    }

}
