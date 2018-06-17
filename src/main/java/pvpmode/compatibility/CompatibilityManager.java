package pvpmode.compatibility;

import java.util.*;

import cpw.mods.fml.common.FMLLog;

/**
 * The compatibility manager manages and loads the compatibility modules.
 * PvPMode and other mods will register their compatibility modules via this
 * manager.
 * 
 * @author CraftedMods
 *
 */
public class CompatibilityManager
{
    private Collection<Class<? extends CompatibilityModuleLoader>> registeredModuleLoaders = new HashSet<> ();

    private boolean areModulesLoaded = false;

    /**
     * Registers the supplied compatibility module loader.<br/>
     * Note that this function can only be called before the registered modules
     * were loaded.
     * 
     * @param moduleLoader
     *            The loader to register
     * @return Whether the loader could be registered
     */
    public boolean registerModuleLoader (Class<? extends CompatibilityModuleLoader> moduleLoader)
    {
        checkState ();
        return this.registeredModuleLoaders.add (moduleLoader);
    }

    /**
     * Unregisters a previously registered compatibility module loader. Note
     * that this function can only be called before the registered modules were
     * loaded.
     * 
     * @param moduleLoader
     *            The loader to unregister
     * @return Whether the loaded could be unregistered
     */
    public boolean unregisterModuleLoader (Class<? extends CompatibilityModuleLoader> moduleLoader)
    {
        checkState ();
        return this.registeredModuleLoaders.remove (moduleLoader);
    }

    /**
     * Loads the registered compatibility modules via the supplied loaders. This
     * function is not intended to be called by other mods.
     */
    public void loadRegisteredModules ()
    {
        checkState ();
        int loadedModulesCounter = 0;
        for (Class<? extends CompatibilityModuleLoader> moduleLoaderClass : registeredModuleLoaders)
        {
            CompatibilityModuleLoader loader = instantiateClass (moduleLoaderClass, "compatibility module loader");
            if (loader != null)
            {
                if (loader.canLoad ())
                {
                    FMLLog.fine (
                        "The compatibility module \"%s\" can be loaded",
                        loader.getModuleName ());

                    try
                    {
                        Class<?> moduleClass = Class.forName (loader.getCompatibilityModuleClassName ());
                        if (CompatibilityModule.class.isAssignableFrom (moduleClass))
                        {
                            CompatibilityModule module = (CompatibilityModule) instantiateClass (moduleClass,
                                "compatibility module");
                            if (module != null)
                            {
                                try
                                {
                                    module.load ();
                                    ++loadedModulesCounter;
                                    FMLLog.getLogger ()
                                        .info (String.format ("The compatibility module \"%s\" was loaded successfully",
                                            loader.getModuleName ()));
                                }
                                catch (Exception e)
                                {
                                    FMLLog.getLogger ().error (String.format (
                                        "The compatibility module \"%s\" couldn't be loaded because of an exception while loading the module",
                                        loader.getModuleName ()), e);
                                }
                            }
                        }
                        else
                        {
                            FMLLog.getLogger ().error (String.format (
                                "The compatibility module class \"%s\" specified by the compatibility module loader \"%s\" is not assignable from \"%s\"",
                                moduleClass.getName (), moduleLoaderClass.getName (),
                                CompatibilityModule.class.getName ()));
                        }
                    }
                    catch (ClassNotFoundException e)
                    {
                        FMLLog.getLogger ()
                            .error (
                                String.format ("The specified compatibility module class \"%s\" couldn't be found",
                                    loader.getCompatibilityModuleClassName ()));
                    }
                }
                else
                {
                    FMLLog.info (
                        "The compatibility module \"%s\" won't be loaded, because it's dependencies are missing",
                        loader.getModuleName ());
                }
            }
        }
        FMLLog.info ("Loaded %d of %d registered compatibility modules", loadedModulesCounter,
            registeredModuleLoaders.size ());
        areModulesLoaded = true;
    }

    private <T> T instantiateClass (Class<T> clazz, String instanceType)
    {
        try
        {
            T instance = clazz.newInstance ();
            return instance;
        }
        catch (InstantiationException e)
        {
            FMLLog.getLogger ().error (String.format ("Couldn't instantiate the %s \"%s\"", instanceType,
                clazz.getName ()), e);
        }
        catch (IllegalAccessException e)
        {
            FMLLog.getLogger ().error (String.format (
                "Couldn't instantiate the %s \"%s\" because there's no accessible default constructor",
                instanceType,
                clazz.getName ()), e);
        }
        return null;
    }

    /**
     * Returns whether the registered compatibility modules were loaded.
     */
    public boolean areModulesLoaded ()
    {
        return areModulesLoaded;
    }

    private void checkState ()
    {
        if (areModulesLoaded)
            throw new IllegalArgumentException ("Invalid state. The compatibility modules were already loaded.");
    }

}
