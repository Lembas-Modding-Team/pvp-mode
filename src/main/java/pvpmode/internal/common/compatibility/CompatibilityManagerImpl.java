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

    private Map<EnumCompatibilityModuleLoadingPoint, Set<CompatibilityModuleLoader>> registeredModuleLoaders = new HashMap<> ();
    private Set<Class<? extends CompatibilityModuleLoader>> registeredModulesLoaderClasses = new HashSet<> ();

    private Map<CompatibilityModuleLoader, CompatibilityModule> loadedModules = new HashMap<> ();
    private Set<Class<? extends CompatibilityModuleLoader>> loadedModulesLoaderClasses = new HashSet<> ();

    public CompatibilityManagerImpl (Path configurationFolder)
    {
        this.configurationFolder = configurationFolder;
        logger = PvPMode.proxy.getLogger ();
    }

    @Override
    public boolean registerModuleLoader (Class<? extends CompatibilityModuleLoader> moduleLoader)
    {
        if (!registeredModulesLoaderClasses.contains (moduleLoader))
        {
            CompatibilityModuleLoader loaderInstance = PvPCommonUtils.createInstance (moduleLoader);

            if (loaderInstance != null)
            {
                EnumCompatibilityModuleLoadingPoint loadingPoint = loaderInstance.getLoadingPoint ();

                if (!registeredModuleLoaders.containsKey (loadingPoint))
                    registeredModuleLoaders.put (loadingPoint, new HashSet<> ());
                registeredModuleLoaders.get (loadingPoint).add (loaderInstance);

                return registeredModulesLoaderClasses.add (moduleLoader);
            }
        }
        return false;

    }

    @Override
    public boolean unregisterModuleLoader (Class<? extends CompatibilityModuleLoader> moduleLoader)
    {
        if (!loadedModulesLoaderClasses.contains (moduleLoader))
        {
            for (Set<CompatibilityModuleLoader> instances : registeredModuleLoaders.values ())
            {
                Iterator<CompatibilityModuleLoader> instanceIterator = instances.iterator ();
                while (instanceIterator.hasNext ())
                {
                    CompatibilityModuleLoader instance = instanceIterator.next ();
                    if (instance.getClass () == moduleLoader)
                    {
                        instanceIterator.remove ();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void loadRegisteredModules (EnumCompatibilityModuleLoadingPoint loadingPoint)
    {
        int loadedModulesCounter = 0;
        Set<CompatibilityModuleLoader> loaders = registeredModuleLoaders.get (loadingPoint);
        
        if (loaders != null)
        {
            for (CompatibilityModuleLoader loader : loaders)
            {
                if (loader.canLoad ())
                {
                    logger.debug (
                        "The compatibility module \"%s\" can be loaded",
                        loader.getModuleName ());

                    loader.onPreLoad ();

                    try
                    {
                        Class<?> moduleClass = Class.forName (loader.getCompatibilityModuleClassName ());
                        if (CompatibilityModule.class.isAssignableFrom (moduleClass))
                        {
                            CompatibilityModule module = (CompatibilityModule) PvPCommonUtils
                                .createInstance (moduleClass);
                            if (module != null)
                            {
                                try
                                {
                                    module.load (loader,
                                        configurationFolder.resolve (loader.getInternalModuleName ()), PvPCommonUtils
                                            .getLogger (logger.getName () + "." + loader.getInternalModuleName ()));
                                    loadedModules.put (loader, module);
                                    loadedModulesLoaderClasses.add (loader.getClass ());
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
                                moduleClass.getName (), loader.getClass ().getName (),
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
            logger.info ("Loaded %d of %d registered compatibility modules on %s", loadedModulesCounter,
                loaders.size (), loadingPoint.name ());
        }
    }

    @Override
    public Map<CompatibilityModuleLoader, CompatibilityModule> getLoadedModules ()
    {
        return loadedModules;
    }

}
