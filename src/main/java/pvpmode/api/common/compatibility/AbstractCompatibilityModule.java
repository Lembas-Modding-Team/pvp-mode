package pvpmode.api.common.compatibility;

import java.io.*;
import java.nio.file.*;

import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;

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

    protected Configuration getDefaultConfiguration () throws IOException
    {
        File configurationFile = configurationFolder.resolve (loader.getInternalModuleName () + ".cfg").toFile ();
        if (!configurationFile.exists ())
            configurationFile.createNewFile ();
        return new Configuration (configurationFile);
    }

}
