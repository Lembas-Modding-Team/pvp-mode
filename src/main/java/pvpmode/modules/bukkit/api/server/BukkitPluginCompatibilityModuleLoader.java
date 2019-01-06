package pvpmode.modules.bukkit.api.server;

import org.bukkit.Bukkit;

import pvpmode.api.common.compatibility.*;
import pvpmode.api.server.compatibility.ServerCompatibilityConstants;

/**
 * A compatibility module loader which determines whether a compatibility module
 * can be loaded by the presence of a Bukkit plugin.
 *
 * @author CraftedMods
 *
 */
public abstract class BukkitPluginCompatibilityModuleLoader extends IdentifierCompatibilityModuleLoader
{
    protected BukkitPluginCompatibilityModuleLoader (String... pluginNames)
    {
        super (pluginNames);
    }

    @Override
    protected boolean isDependencyLoaded (String identifier)
    {
        return Bukkit.getPluginManager ().isPluginEnabled (identifier);
    }

    @Override
    public EnumCompatibilityModuleLoadingPoint getLoadingPoint ()
    {
        return ServerCompatibilityConstants.SERVER_STARTING_LOADING_POINT;
    }

}
