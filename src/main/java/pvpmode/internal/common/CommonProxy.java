package pvpmode.internal.common;

import java.nio.file.*;

import org.apache.commons.lang3.tuple.Pair;

import cpw.mods.fml.common.event.*;
import net.minecraftforge.common.config.Configuration;
import pvpmode.*;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.common.configuration.*;
import pvpmode.api.common.version.*;
import pvpmode.internal.common.compatibility.CompatibilityManagerImpl;
import pvpmode.internal.common.configuration.*;
import pvpmode.internal.common.core.PvPModeCore;
import pvpmode.internal.common.utils.ClassDiscoverer;
import pvpmode.internal.common.version.VersionCheckerImpl;

public class CommonProxy implements Configurable
{

    protected Configuration forgeConfiguration;
    protected Path configurationFolder;
    protected Path generatedFilesFolder;
    protected CommonConfiguration configuration;
    protected CompatibilityManagerImpl compatibilityManager;
    protected final ClassDiscoverer discoverer = PvPModeCore.getInstance ().getClassDiscoverer ();

    protected SimpleLogger logger;

    protected AutoConfigurationCreator autoConfigManager;
    protected final AutoConfigurationMapperManager autoConfigMapperManager = PvPModeCore.getInstance ()
        .getAutoConfigurationMapperManager ();

    protected VersionChecker versionChecker = new VersionCheckerImpl (
        "https://raw.githubusercontent.com/Lembas-Modding-Team/pvp-mode/development/version.txt");// TODO Change with
                                                                                                  // release

    private RemoteVersion remoteVersion;
    private EnumVersionComparison versionComparison;

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

        compatibilityManager.loadRegisteredModules (EnumCompatibilityModuleLoadingPoint.PRE_INIT);
    }

    protected void registerCompatibilityModules ()
    {
    }

    public void onInit (FMLInitializationEvent event) throws Exception
    {
        if (configuration.isVersionCheckerEnabled ())
        {
            Pair<RemoteVersion, EnumVersionComparison> result = versionChecker.checkVersion (PvPMode.SEMANTIC_VERSION);
            remoteVersion = result.getKey ();
            versionComparison = result.getValue ();

            if (remoteVersion != null)
            {
                logger.info ("Found a remote version for the PvP Mode Mod: %s (%s version)",
                    remoteVersion.getRemoteVersion ().toString (),
                    versionComparison);
            }
        }

        compatibilityManager.loadRegisteredModules (EnumCompatibilityModuleLoadingPoint.INIT);
    }

    public void onPostInit (FMLPostInitializationEvent event) throws Exception
    {
        compatibilityManager.loadRegisteredModules (EnumCompatibilityModuleLoadingPoint.POST_INIT);
    }

    public void onLoadingComplete (FMLLoadCompleteEvent event)
    {
        compatibilityManager.loadRegisteredModules (EnumCompatibilityModuleLoadingPoint.LOADING_COMPLETED);
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

    public RemoteVersion getRemoteVersion ()
    {
        return remoteVersion;
    }

    public EnumVersionComparison getVersionComparison ()
    {
        return versionComparison;
    }

}
