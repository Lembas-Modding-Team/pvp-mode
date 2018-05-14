package pvpmode.log;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A basic pvp logging module which supports files which are rotated every time
 * the server starts. You can use this as superclass for logging modules which
 * store files on the filesystem.
 * 
 * @author CraftedMods
 *
 */
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
            // The file where the latest data will be stored
            Path logFile = pvpLoggingDir.resolve (configName).resolve ("pvpmode_latest." + fileEnding);

            if (!Files.exists (logFile))
            {
                Files.createDirectories (logFile.getParent ());
                Files.createFile (logFile);
            }
            else
            {
                // The last "latest" logging file will be renamed
                Files.move (logFile, this.getUnusedFileNameWithIndex (logFile.resolveSibling (
                                "pvpmode_old_" + new SimpleDateFormat ("yyyy-MM-dd").format (new Date ())
                                                .toString ()),
                                fileEnding));
            }

            writer = new PrintWriter (Files.newBufferedWriter (logFile), true);

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
        int i = 1;
        do
        {
            result = Paths.get (String.format("%s_%d.%s",basicFileNameWithoutFileEnding.toString (),i++,fileEnding));
        }
        while (Files.exists (result));
        return result;
    }

    @Override
    public void cleanup()
    {
        writer.flush ();
        writer.close ();

    }

}
