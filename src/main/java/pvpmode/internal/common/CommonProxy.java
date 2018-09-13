package pvpmode.internal.common;

import java.nio.file.*;

import cpw.mods.fml.common.event.*;
import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.CompatibilityManager;
import pvpmode.api.common.configuration.CommonConfiguration;
import pvpmode.internal.common.compatibility.CompatibilityManagerImpl;

public class CommonProxy
{

    protected Configuration forgeConfiguration;
    protected Path configurationFolder;
    protected CommonConfiguration configuration;
    protected CompatibilityManagerImpl compatibilityManager;

    protected SimpleLogger logger;

    public void onPreInit (FMLPreInitializationEvent event) throws Exception
    {
        logger = new SimpleLoggerImpl (event.getModLog ());

        configurationFolder = event.getSuggestedConfigurationFile ().getParentFile ().toPath ().resolve ("pvp-mode");

        Files.createDirectories (configurationFolder);

        forgeConfiguration = new Configuration (configurationFolder.resolve ("pvp-mode.cfg").toFile ());

        compatibilityManager = new CompatibilityManagerImpl (configurationFolder);

        registerCompatibilityModules ();
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

}
