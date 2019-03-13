package pvpmode.api.common.compatibility;

import java.io.*;
import java.nio.file.*;
import java.util.function.Function;

import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.ConfigurationManager;

public abstract class AbstractCompatibilityModule implements CompatibilityModule
{

    protected CompatibilityModuleLoader loader;
    protected Path configurationFolder;
    protected SimpleLogger logger;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        this.loader = loader;
        this.configurationFolder = configurationFolder;
        this.logger = logger;

        if (!Files.exists (configurationFolder))
            Files.createDirectories (configurationFolder);

    }

    protected Configuration createForgeConfigurationFile () throws IOException
    {
        File configurationFile = configurationFolder.resolve (loader.getInternalModuleName () + ".cfg").toFile ();
        if (!configurationFile.exists ())
            configurationFile.createNewFile ();
        return new Configuration (configurationFile);
    }

    protected <T extends ConfigurationManager, U extends T> T createConfiguration (
        Function<Configuration, U> configurationManagerCreator)
        throws IOException
    {
        Configuration forgeConfiguration = this.createForgeConfigurationFile ();
        T configurationManager = configurationManagerCreator.apply (forgeConfiguration);
        configurationManager.load ();
        return configurationManager;
    }

    public SimpleLogger getLogger ()
    {
        return logger;
    }

}
