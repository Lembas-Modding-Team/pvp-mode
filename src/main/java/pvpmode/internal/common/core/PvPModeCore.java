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
    private final SimpleLogger logger = new SimpleLoggerImpl (LogManager.getLogger ("pvp-mode-core"));

    private String[] classTransformerClassNames = null;

    private static PvPModeCore instance;

    public PvPModeCore ()
    {
        instance = this;

        PvPCommonUtils.setProvider (new PvPCommonUtilsProvider ());

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
        ArrayList<String> classes = new ArrayList<> ();

        Map<String, Set<String>> registerClasses = classDiscoverer.getDiscoveredClassNames (30000l)
            .get (Register.class);

        Set<String> registeredClassTransformers = registerClasses.get (IClassTransformer.class.getName ());

        if (registeredClassTransformers != null)
        {
            for (String classTransformerClassName : registeredClassTransformers)
            {
                try
                {
                    boolean load = true;

                    Class<?> classTransformerClass = Loader.instance ().getModClassLoader ()
                        .loadClass (classTransformerClassName);

                    Map<String, String> properties = PvPCommonUtils
                        .getPropertiesFromRegisteredClass (classTransformerClass);

                    // If no side is specified, COMMON is assumed
                    if (properties.containsKey (CoremodEnvironmentConstants.SIDE_KEY))
                    {
                        String specifiedSide = properties.get (CoremodEnvironmentConstants.SIDE_KEY);

                        switch (specifiedSide)
                        {
                            case CoremodEnvironmentConstants.SIDE_CLIENT:
                                load = isServerside () == false;
                                break;
                            case CoremodEnvironmentConstants.SIDE_COMMON:
                                break;
                            case CoremodEnvironmentConstants.SIDE_SERVER:
                                load = isServerside () == true;
                                break;
                            default:
                                logger.error (
                                    "The specified side \"%s\" of the class transformer \"%s\" is invalid (must be %s, %s or %s). The transformer won't be loaded.",
                                    specifiedSide, classTransformerClassName, CoremodEnvironmentConstants.SIDE_CLIENT,
                                    CoremodEnvironmentConstants.SIDE_COMMON,
                                    CoremodEnvironmentConstants.SIDE_SERVER);
                                load = false;
                                break;
                        }
                    }

                    if (load)
                    {
                        classes.add (classTransformerClassName);
                    }
                }
                catch (ClassNotFoundException e)
                {
                    logger.errorThrowable ("Couldn't load the class transformer class \"%s\"", e,
                        classTransformerClassName);
                }
            }
            logger.info ("Loaded %d of %d registered class transformers", classes.size (),
                registeredClassTransformers == null ? 0 : registeredClassTransformers.size ());
            classTransformerClassNames = classes.stream ()
                .toArray (size -> new String[size]);
        }
    }

    @Override
    public String getModContainerClass ()
    {
        return null;
    }

    @Override
    public String getSetupClass ()
    {
        return null;
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

}
