package pvpmode.api.common.compatibility;

import java.lang.reflect.*;

import cpw.mods.fml.common.FMLLog;

/**
 * A compatibility module loader which determines whether a compatibility module
 * can be loaded by the presence of a Bukkit plugin.
 *
 * @author CraftedMods
 *
 */
public abstract class BukkitPluginCompatibilityModuleLoader extends IdentifierCompatibilityModuleLoader
{
    private boolean triedLoadingBukkit;
    private Object pluginManager;
    private Method isPluginEnabledFunction;

    protected BukkitPluginCompatibilityModuleLoader (String... pluginNames)
    {
        super (pluginNames);
    }

    /*
     * TODO: The whole method is just ugly. With a future version the compatibility
     * module system will be improved and this ugly code will be history then.
     */
    @Override
    protected boolean isDependencyLoaded (String identifier)
    {
        if (!triedLoadingBukkit)
        {
            try
            {
                Class<?> bukkitClass = Class.forName ("org.bukkit.Bukkit"); // Check whether Bukkit is present
                Class.forName ("org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack");// Check whether CraftBukkit
                                                                                          // is present
                try
                {
                    // Retrieve the method to determine whether a Bukkit plugin is enabled
                    Method getPluginManager = bukkitClass.getDeclaredMethod ("getPluginManager");
                    pluginManager = getPluginManager.invoke (bukkitClass);
                    Class<?> pluginManagerClass = pluginManager.getClass ();
                    isPluginEnabledFunction = pluginManagerClass.getMethod ("isPluginEnabled", String.class);
                }
                catch (NoSuchMethodException e)
                {
                    FMLLog.getLogger ().error ("Couldn't find the specified Bukkit method", e);
                }
                catch (SecurityException e)
                {
                    FMLLog.getLogger ().error ("Couldn't access the specified Bukkit method", e);
                }

            }
            catch (Exception | NoClassDefFoundError e)
            {
                FMLLog.info ("CraftBukkit isn't present");
            }
            triedLoadingBukkit = true;
        }
        return isPluginEnabled (identifier);
    }

    private boolean isPluginEnabled (String identifier)
    {
        if (isPluginEnabledFunction != null)
        {
            try
            {
                return (boolean) isPluginEnabledFunction.invoke (pluginManager, identifier);
            }
            catch (IllegalAccessException e)
            {
                FMLLog.getLogger ().error ("Couldn't access the specified Bukkit method", e);
            }
            catch (IllegalArgumentException e)
            {
                FMLLog.getLogger ().error ("The specified Bukkit method was used wrongly", e);
            }
            catch (InvocationTargetException e)
            {
                FMLLog.getLogger ().error ("The specified Bukkit method threw an exception", e);
            }
        }
        return false;
    }

}
