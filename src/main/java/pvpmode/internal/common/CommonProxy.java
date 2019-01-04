package pvpmode.internal.common;

import java.nio.file.*;

import cpw.mods.fml.common.event.*;
import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.CompatibilityManager;
import pvpmode.api.common.configuration.*;
import pvpmode.internal.common.compatibility.CompatibilityManagerImpl;
import pvpmode.internal.common.configuration.*;
import pvpmode.internal.common.core.PvPModeCore;
import pvpmode.internal.common.utils.ClassDiscoverer;

public class CommonProxy implements Configurable
{

    protected Configuration forgeConfiguration;
    protected Path configurationFolder;
    protected Path generatedFilesFolder;
    protected CommonConfiguration configuration;
    protected CompatibilityManagerImpl compatibilityManager;
    protected final ClassDiscoverer discoverer = PvPModeCore.classDiscoverer;

    protected SimpleLogger logger;

    protected AutoConfigurationCreator autoConfigManager;
    protected final AutoConfigurationMapperManager autoConfigMapperManager = PvPModeCore.autoConfigurationMapperManager;

    public void onPreInit (FMLPreInitializationEvent event) throws Exception
    {
        logger = new SimpleLoggerImpl (event.getModLog ());

        configurationFolder = event.getSuggestedConfigurationFile ().getParentFile ().toPath ().resolve ("pvp-mode");

        Files.createDirectories (configurationFolder);

        generatedFilesFolder = event.getSuggestedConfigurationFile ().getParentFile ().getParentFile ().toPath ()
            .resolve ("pvp-mode");

        Files.createDirectories (generatedFilesFolder);

        forgeConfiguration = new Configuration (configurationFolder.resolve ("pvp-mode.cfg").toFile ());

        compatibilityManager = new CompatibilityManagerImpl (configurationFolder);

        registerCompatibilityModules ();

        autoConfigManager = new AutoConfigurationCreator ();

        autoConfigManager.processClasspath (discoverer, 30000);
    }

    protected void registerCompatibilityModules ()
    {
    }

    public void onInit (FMLInitializationEvent event) throws Exception
    {
        compatibilityManager.loadRegisteredModules ();
    }

    public void onPostInit (FMLPostInitializationEvent event) throws Exception
    {

    }

    public CommonConfiguration getConfiguration ()
    {
        return configuration;
    }

    public CompatibilityManager getCompatibilityManager ()
    {
        return compatibilityManager;
    }

    public SimpleLogger getLogger ()
    {
        return logger;
    }

    public AutoConfigurationCreator getAutoConfigManager ()
    {
        return autoConfigManager;
    }

    public AutoConfigurationMapperManager getAutoConfigMapperManager ()
    {
        return autoConfigMapperManager;
    }

    public Path getGeneratedFilesFolder ()
    {
        return generatedFilesFolder;
    }

}
