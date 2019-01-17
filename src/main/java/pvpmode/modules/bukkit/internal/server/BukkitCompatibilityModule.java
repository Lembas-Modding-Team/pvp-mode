package pvpmode.modules.bukkit.internal.server;

import java.nio.file.Path;

import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.modules.citizens.internal.server.CitizensCompatibilityModuleLoader;
import pvpmode.modules.deathcraft.internal.server.DeathcraftCompatibilityModuleLoader;

public class BukkitCompatibilityModule implements CompatibilityModule
{

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        CompatibilityManager compatibilityManager = PvPMode.proxy.getCompatibilityManager ();
        
        compatibilityManager.registerModuleLoader (DeathcraftCompatibilityModuleLoader.class);
        compatibilityManager.registerModuleLoader (CitizensCompatibilityModuleLoader.class);
    }

}
