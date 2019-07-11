package pvpmode.modules.lotr.internal.server;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

import lotr.common.fac.LOTRFaction;
import pvpmode.api.common.SimpleLogger;
import pvpmode.modules.lotr.api.common.LOTRCommonUtils;
import pvpmode.modules.lotr.api.server.*;

public abstract class FactionEntryParser
{

    protected final String configName;
    protected final Path file;
    protected final SimpleLogger logger;
    protected final LOTRServerConfiguration config;

    public FactionEntryParser (String configName, Path file, SimpleLogger logger, LOTRServerConfiguration config)
    {
        this.configName = configName;
        this.file = file;
        this.logger = logger;
        this.config = config;
    }

    public void parse () throws IOException
    {
        // Parse and load the config file
        int validEntryCounter = 0;
        int invalidEntryCounter = 0;
        try (BufferedReader reader = Files.newBufferedReader (file))
        {
            // Read the file line by line
            String line = null;
            for (int i = 1; (line = reader.readLine ()) != null; i++)
            {
                line = line.trim ();
                // Ignore comments and empty lines
                if (!line.isEmpty () && !line.startsWith ("#"))
                {
                    // Split config entries into three columns
                    String[] parts = line.split (";");
                    if (parts.length < 3)
                    {
                        // There are less than three columns
                        ++invalidEntryCounter;
                        logger.warning (
                            "The %s config entry \"%s\" at line %d is invalid. There're too less columns separated by semicolons.",
                            configName,
                            line,
                            i);
                    }
                    else
                    {
                        // Extract the faction identifier from the first column
                        String faction = parts[0].trim ();
                        if (faction.equals (LOTRServerConstants.FACTION_ENTRY_WILDCARD)
                            || LOTRFaction.forName (faction) != null
                            || config.getFactionPlaceholders ().containsKey (faction))
                        {

                            String alignmentString = parts[1].trim ();
                            try
                            {
                                // Extract the minimum alignment from the second column
                                Integer alignmentInt = Integer.parseInt (alignmentString);

                                // Add the data to our data structures
                                FactionEntry entry = new FactionEntry (faction, LOTRCommonUtils
                                    .getAllFactionsOfPlaceholder (config.getFactionPlaceholders (), faction),
                                    alignmentInt);

                                if (parseLine (entry, i, Arrays.copyOfRange (parts, 2, parts.length)))
                                {
                                    ++validEntryCounter;
                                }
                                else
                                {
                                    ++invalidEntryCounter;
                                    break;
                                }
                            }
                            catch (NumberFormatException e)
                            {
                                ++invalidEntryCounter;
                                logger.warning (
                                    "The %s config entry at line %d contains an invalid minimum alignment value (\"%s\")",
                                    configName,
                                    i, alignmentString);
                            }
                        }
                        else
                        {
                            // The faction name is invalid
                            ++invalidEntryCounter;
                            logger.warning (
                                "The %s config entry at line %d contains an invalid faction name (\"%s\").", configName,
                                i, faction);
                        }
                    }
                }
            }
        }

        logger.info ("Loaded %d of %d specified %s config entries. %d config entries are invalid", validEntryCounter,
            invalidEntryCounter + validEntryCounter, configName, invalidEntryCounter);
    }

    /**
     * Parses the rest of a line at the configuration file.
     * 
     * @param entry
     *            The faction entry
     * @param remainingParts
     *            The remaining parts of the line
     * @return Whether the line was valid
     */
    protected abstract boolean parseLine (FactionEntry entry, int line, String[] remainingParts);

    protected <T> Collection<T> parseList (String entryTypeName, String list, int line,
        Function<String, T> entryParser)
    {
        // Extract the list entries
        String[] listEntries = list.trim ().split (",");
        if (listEntries.length <= 0)
        {
            // No entries were specified
            logger.warning (
                "The %s config entry at line %d contains no assigned %ss", configName,
                line, entryTypeName);
            return null;
        }
        else
        {
            // Parse the entries
            Collection<T> parsedEntries = new HashSet<> ();
            for (String listEntry : listEntries)
            {
                String trimmedListEntry = listEntry.trim ();

                T parsedEntry = entryParser.apply (trimmedListEntry);

                if (parsedEntry != null)
                {
                    if (!parsedEntries.add (parsedEntry))
                    {
                        // Duplicated entries specified
                        logger.warning (
                            "The %s config entry at line %d contains a duplicated %s (%s).",
                            configName,
                            line, entryTypeName, trimmedListEntry);
                    }
                }
                else
                {
                    logger.warning (
                        "The %s config entry at line %d contains an invalid %s (\"%s\"). The %3$s will be ignored.",
                        configName,
                        line, entryTypeName, trimmedListEntry);
                }
            }
            if (parsedEntries.isEmpty ())
            {
                // Entries were specified, but all were invalid
                logger.warning (
                    "The %s config entry at line %d contains only invalid %ss. The entry will be ignored.",
                    configName,
                    line, entryTypeName);
                return null;
            }
            return parsedEntries;
        }
    }

}
