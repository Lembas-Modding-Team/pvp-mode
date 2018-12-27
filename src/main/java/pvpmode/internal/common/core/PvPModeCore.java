package pvpmode.internal.common.core;

import java.util.Map;

import org.apache.logging.log4j.LogManager;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.*;
import pvpmode.api.common.utils.*;
import pvpmode.api.common.utils.Process;
import pvpmode.internal.common.SimpleLoggerImpl;
import pvpmode.internal.common.configuration.AutoConfigurationMapperManager;
import pvpmode.internal.common.utils.*;

@MCVersion(value = "1.7.10")
@SortingIndex(value = 15000)
@TransformerExclusions(value =
{"pvpmode.internal.common.core", "pvpmode.internal.common.configuration"})
public class PvPModeCore implements IFMLLoadingPlugin
{
    
    static boolean obfuscatedEnvironment = true;

    public static final ClassDiscoverer classDiscoverer;

    public static final AutoConfigurationMapperManager autoConfigurationMapperManager;

    static
    {
        PvPCommonUtils.setProvider (new PvPCommonUtilsProvider ());

        classDiscoverer = new ClassDiscoverer (
            new SimpleLoggerImpl (LogManager.getLogger (ClassDiscoverer.class)));

        classDiscoverer.registerClassToDiscover (Register.class);
        classDiscoverer.registerClassToDiscover (Process.class);
        classDiscoverer.registerClassToDiscover (Inject.class);

        classDiscoverer.discoverClassesAsync ();

        autoConfigurationMapperManager = new AutoConfigurationMapperManager ();
    }

    @Override
    public String[] getASMTransformerClass ()
    {
        return new String[]
        {PvPModeClassTransformer.class.getName (), AutoConfigurationTransformer.class.getName ()};
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

}
