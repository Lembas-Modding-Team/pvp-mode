package pvpmode.internal.common.compatibility;

import java.nio.file.Path;
import java.util.*;

import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.common.utils.PvPCommonUtils;

public class CompatibilityManagerImpl implements CompatibilityManager
{
    private final Path configurationFolder;

    private final SimpleLogger logger;

    private Collection<Class<? extends CompatibilityModuleLoader>> registeredModuleLoaders = new HashSet<> ();

    private boolean areModulesLoaded = false;

    private Map<CompatibilityModuleLoader, CompatibilityModule> loadedModules = new HashMap<> ();

    public CompatibilityManagerImpl (Path configurationFolder)
    {
        this.configurationFolder = configurationFolder;
        logger = PvPMode.proxy.getLogger ();
    }

    @Override
    public boolean registerModuleLoader (Class<? extends CompatibilityModuleLoader> moduleLoader)
    {
        checkState ();
        return registeredModuleLoaders.add (moduleLoader);
    }

    @Override
    public boolean unregisterModuleLoader (Class<? extends CompatibilityModuleLoader> moduleLoader)
    {
        checkState ();
        return registeredModuleLoaders.remove (moduleLoader);
    }

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
                    logger.debug (
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
                                    module.load (loader,
                                        configurationFolder.resolve (loader.getInternalModuleName ()), PvPCommonUtils
                                            .getLogger (logger.getName () + "." + loader.getInternalModuleName ()));
                                    loadedModules.put (loader, module);
                                    ++loadedModulesCounter;
                                    logger.info ("The compatibility module \"%s\" was loaded successfully",
                                        loader.getModuleName ());
                                }
                                catch (Exception e)
                                {
                                    logger.errorThrowable (
                                        "The compatibility module \"%s\" couldn't be loaded because of an exception while loading the module",
                                        e,
                                        loader.getModuleName ());
                                }
                            }
                        }
                        else
                        {
                            logger.error (
                                "The compatibility module class \"%s\" specified by the compatibility module loader \"%s\" is not assignable from \"%s\"",
                                moduleClass.getName (), moduleLoaderClass.getName (),
                                CompatibilityModule.class.getName ());
                        }
                    }
                    catch (ClassNotFoundException e)
                    {
                        logger
                            .error ("The specified compatibility module class \"%s\" couldn't be found",
                                loader.getCompatibilityModuleClassName ());
                    }
                }
                else
                {
                    logger.debug (
                        "The compatibility module \"%s\" won't be loaded, because it's dependencies are missing",
                        loader.getModuleName ());
                }
            }
        }
        logger.info ("Loaded %d of %d registered compatibility modules", loadedModulesCounter,
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
            logger.errorThrowable ("Couldn't instantiate the %s \"%s\"", e, instanceType,
                clazz.getName ());
        }
        catch (IllegalAccessException e)
        {
            logger.errorThrowable (
                "Couldn't instantiate the %s \"%s\" because there's no accessible default constructor", e,
                instanceType,
                clazz.getName ());
        }
        return null;
    }

    @Override
    public boolean areModulesLoaded ()
    {
        return areModulesLoaded;
    }

    @Override
    public Map<CompatibilityModuleLoader, CompatibilityModule> getLoadedModules ()
    {
        return loadedModules;
    }

    private void checkState ()
    {
        if (areModulesLoaded)
            throw new IllegalArgumentException ("Invalid state. The compatibility modules were already loaded.");
    }

}
