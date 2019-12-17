package pvpmode.internal.common.core;

import java.util.*;

import org.apache.logging.log4j.LogManager;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.*;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.*;
import net.minecraft.launchwrapper.IClassTransformer;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.core.CoremodEnvironmentConstants;
import pvpmode.api.common.utils.*;
import pvpmode.api.common.utils.Process;
import pvpmode.internal.common.SimpleLoggerImpl;
import pvpmode.internal.common.configuration.AutoConfigurationMapperManager;
import pvpmode.internal.common.utils.*;

@MCVersion(value = "1.7.10")
@SortingIndex(value = 15000)
@TransformerExclusions(value =
{"pvpmode.internal.common.core"})
public class PvPModeCore implements IFMLLoadingPlugin
{

    private boolean obfuscatedEnvironment; // TODO integrate with DI (postInit)

    private final ClassDiscoverer classDiscoverer;
    private final AutoConfigurationMapperManager autoConfigurationMapperManager;
    static final SimpleLogger logger = new SimpleLoggerImpl (LogManager.getLogger ("pvp-mode-core"));

    private String[] classTransformerClassNames = null;

    private static PvPModeCore instance;

    public PvPModeCore ()
    {
        instance = this;

        PvPCommonCoreUtils.setProvider (new PvPCommonCoreUtilsProvider ());

        classDiscoverer = new ClassDiscoverer (
            new SimpleLoggerImpl (LogManager.getLogger (ClassDiscoverer.class)));

        classDiscoverer.registerClassToDiscover (Register.class);
        classDiscoverer.registerClassToDiscover (Process.class);
        classDiscoverer.registerClassToDiscover (Inject.class);

        classDiscoverer.discoverClassesAsync ();

        autoConfigurationMapperManager = new AutoConfigurationMapperManager ();
    }

    public boolean isServerside ()
    {
        return FMLLaunchHandler.side () == Side.SERVER;
    }

    public boolean isObfuscatedEnvironment ()
    {
        return obfuscatedEnvironment;
    }

    public ClassDiscoverer getClassDiscoverer ()
    {
        return classDiscoverer;
    }

    public AutoConfigurationMapperManager getAutoConfigurationMapperManager ()
    {
        return autoConfigurationMapperManager;
    }

    public SimpleLogger getLogger ()
    {
        return logger;
    }

    @Override
    public String[] getASMTransformerClass ()
    {
        if (classTransformerClassNames == null)
        {
            loadClassTransformers ();
        }
        return classTransformerClassNames;
    }

    /*
     * Tries to discover the class transformers of the PvP mode Mod. They are marked
     * with the @Register annotation, and can optionally contain data about the side
     * where they should be loaded. If no side is specified, COMMON is assumed.
     */
    private void loadClassTransformers ()
    {
        classTransformerClassNames = retrieveExtensions (instance, IClassTransformer.class, "class transformer")
            .stream ().map (Class::getName)
            .toArray (size -> new String[size]);
    }

    /**
     * A helper function useful for loading classes with a specified interface
     * annotated with {@link Register}. Thereby a parameter "side" will be taken
     * into account, which specifies the side on which this extension should be
     * loaded. By default common is assumed
     * 
     * @param core
     *            The instance of the coremod
     * @param extensionClassInterface
     *            The interface of the extension class
     * @param extensionConsoleName
     *            A name that will be used in the logs for the extension
     * @return A list of class names matching the specified criteria and side
     */
    @SuppressWarnings("unchecked")
    private static <T> Collection<Class<? extends T>> retrieveExtensions (PvPModeCore core,
        Class<T> extensionClassInterface,
        String extensionConsoleName)
    {
        ArrayList<Class<? extends T>> extensions = new ArrayList<> ();

        Map<String, Set<String>> registerClasses = core.classDiscoverer.getDiscoveredClassNames (30000l)
            .get (Register.class);

        Set<String> registeredExtensions = registerClasses.get (extensionClassInterface.getName ());

        if (registeredExtensions != null)
        {
            for (String extensionClassName : registeredExtensions)
            {
                try
                {
                    boolean load = true;

                    Class<?> extensionClass = Loader.instance ().getModClassLoader ()
                        .loadClass (extensionClassName);

                    Map<String, String> properties = PvPCommonCoreUtils
                        .getPropertiesFromRegisteredClass (extensionClass);

                    // If no side is specified, COMMON is assumed
                    if (properties.containsKey (CoremodEnvironmentConstants.SIDE_KEY))
                    {
                        String specifiedSide = properties.get (CoremodEnvironmentConstants.SIDE_KEY);

                        switch (specifiedSide)
                        {
                            case CoremodEnvironmentConstants.SIDE_CLIENT:
                                load = core.isServerside () == false;
                                break;
                            case CoremodEnvironmentConstants.SIDE_COMMON:
                                break;
                            case CoremodEnvironmentConstants.SIDE_SERVER:
                                load = core.isServerside () == true;
                                break;
                            default:
                                logger.error (
                                    "The specified side \"%s\" of the %s \"%s\" is invalid (must be %s, %s or %s). The extension won't be loaded.",
                                    specifiedSide, extensionConsoleName, extensionClassName,
                                    CoremodEnvironmentConstants.SIDE_CLIENT,
                                    CoremodEnvironmentConstants.SIDE_COMMON,
                                    CoremodEnvironmentConstants.SIDE_SERVER);
                                load = false;
                                break;
                        }
                    }

                    if (load)
                    {
                        extensions.add ((Class<? extends T>) extensionClass);
                    }
                }
                catch (ClassNotFoundException e)
                {
                    logger.errorThrowable ("Couldn't load the %s class \"%s\"", e, extensionConsoleName,
                        extensionClassName);
                }
            }
            logger.info ("Loaded %d of %d registered %s", extensions.size (),
                registeredExtensions == null ? 0 : registeredExtensions.size (), extensionConsoleName);
        }
        return extensions;
    }

    @Override
    public String getModContainerClass ()
    {
        return null;
    }

    @Override
    public String getSetupClass ()
    {
        return PvPModeCoreSetup.class.getName ();
    }

    @Override
    public void injectData (Map<String, Object> data)
    {
        obfuscatedEnvironment = (boolean) data.get ("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass ()
    {
        return null;
    }

    public static PvPModeCore getInstance ()
    {
        return instance;
    }

    /*
     * Allows for modules to specify call hooks which will be collected here.
     */
    public static class PvPModeCoreSetup implements IFMLCallHook
    {

        private final Collection<IFMLCallHook> hooks = new ArrayList<> ();

        public PvPModeCoreSetup ()
        {
            Collection<Class<? extends IFMLCallHook>> setupClasses = retrieveExtensions (instance, IFMLCallHook.class,
                "call hook");
            hooks.addAll (PvPCommonCoreUtils.createInstances (setupClasses));
        }

        @Override
        public Void call () throws Exception
        {
            for (IFMLCallHook hook : hooks)
            {
                hook.call ();
            }

            return null;

        }

        @Override
        public void injectData (Map<String, Object> data)
        {
            for (IFMLCallHook hook : hooks)
            {
                hook.injectData (data);
            }
        }

    }

}
