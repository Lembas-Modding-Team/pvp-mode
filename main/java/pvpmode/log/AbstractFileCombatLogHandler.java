package pvpmode.log;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractFileCombatLogHandler implements CombatLogHandler
{

    private final String configName;
    private final String fileEnding;

    protected PrintWriter writer;

    protected AbstractFileCombatLogHandler(String configName, String fileEnding)
    {
        Objects.requireNonNull (configName);
        Objects.requireNonNull (fileEnding);

        this.configName = configName;
        this.fileEnding = fileEnding;
    }

    @Override
    public void init(Path pvpLoggingDir)
    {
        try
        {
            Path logFile = pvpLoggingDir.resolve (configName).resolve ("pvpmode_latest." + fileEnding);

            if (!Files.exists (logFile))
            {
                Files.createDirectories (logFile.getParent ());
                Files.createFile (logFile);
            }
            else
            {
                Files.move (logFile, this.getUnusedFileNameWithIndex (logFile.resolveSibling (
                                "pvpmode_old_" + new SimpleDateFormat ("yyyy-MM-dd").format (new Date ())
                                                .toString ()),
                                fileEnding));
            }

            writer = new PrintWriter (logFile.toFile ());

        }
        catch (IOException e)
        {
            throw new RuntimeException (String.format (
                            "The pvp log file of the handler \"%s\" couldn't be created or accessed", configName), e);
        }

    }

    protected Path getUnusedFileNameWithIndex(Path basicFileNameWithoutFileEnding, String fileEnding)
    {
        Path result = basicFileNameWithoutFileEnding;
        for (int i = 1; Files.exists (result = Paths
                        .get (basicFileNameWithoutFileEnding.toString () + "_" + i + "." + fileEnding)); i++)
            ;
        return result;
    }

    @Override
    public void cleanup()
    {
        writer.close ();

    }

}
