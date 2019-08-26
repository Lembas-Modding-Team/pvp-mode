package pvpmode.modules.bukkit.internal.server;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import pvpmode.api.common.compatibility.*;

public class BukkitCompatibilityModuleLoader implements CompatibilityModuleLoader
{

    @Override
    public String getModuleName ()
    {
        return "Bukkit Compatibility";
    }

    @Override
    public String getInternalModuleName ()
    {
        return "bukkit";
    }

    @Override
    public String getCompatibilityModuleClassName (Side side)
    {
        return "pvpmode.modules.bukkit.internal.server.BukkitCompatibilityModule";
    }

    @Override
    public boolean canLoad ()
    {
        try
        {
            Class.forName ("org.bukkit.Bukkit"); // Check whether Bukkit is present
            Class.forName ("org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack");// Check whether CraftBukkit is
                                                                                      // present

            return Loader.isModLoaded ("kimagine"); // Check whether we're at cauldron
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public EnumCompatibilityModuleLoadingPoint getLoadingPoint ()
    {
        return EnumCompatibilityModuleLoadingPoint.PRE_INIT;
    }

    @Override
    public void onPreLoad ()
    {
    }

}
