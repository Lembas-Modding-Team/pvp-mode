package pvpmode.modules.lotr.internal.server;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

import lotr.common.fac.LOTRFaction;
import pvpmode.api.common.SimpleLogger;
import pvpmode.modules.lotr.api.common.*;
import pvpmode.modules.lotr.api.server.*;
import pvpmode.modules.lotr.internal.common.FactionEntry;

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
                        if (faction.equals (LOTRCommonConstants.FACTION_ENTRY_WILDCARD)
                            || LOTRFaction.forName (faction) != null
                            || config.getFactionPlaceholders ().containsKey (faction))
                        {

                            // Extract the alignment values
                            String alignmentString = parts[1].trim ();

                            if (!alignmentString.isEmpty ())
                            {
                                String[] alignmentStringParts = alignmentString
                                    .split (LOTRServerConstants.FACTION_ALIGNMENT_SEPARATOR, -1);

                                String firstPart = alignmentStringParts[0].trim ();
                                String secondPart = null;

                                if (alignmentStringParts.length > 1)
                                {
                                    secondPart = alignmentStringParts[1].trim ();
                                }

                                if (alignmentStringParts.length <= 2) // More than two parts are not allowed
                                {

                                    try
                                    {
                                        Integer alignmentWithoutPledging = firstPart.isEmpty () ? null
                                            : Integer.parseInt (firstPart);
                                        Integer alignmentWithPledging = (secondPart == null || secondPart.isEmpty ())
                                            ? null
                                            : Integer.parseInt (secondPart);

                                        if (alignmentWithoutPledging != null || alignmentWithPledging != null)
                                        {

                                            // Gather the remaining data and delegate the parsing to the subclass

                                            boolean success = true; // The subclass needs to accept both entries, if
                                                                    // present

                                            Set<String> placeholders = LOTRCommonUtils
                                                .getAllFactionsOfPlaceholder (config.getFactionPlaceholders (),
                                                    faction);

                                            if (alignmentWithoutPledging != null)
                                            {
                                                success = parseLine (new FactionEntry (faction, placeholders,
                                                    alignmentWithoutPledging, false), i,
                                                    Arrays.copyOfRange (parts, 2, parts.length));
                                            }

                                            if (alignmentWithPledging != null)
                                            {
                                                success = success && parseLine (new FactionEntry (faction, placeholders,
                                                    alignmentWithPledging, true), i,
                                                    Arrays.copyOfRange (parts, 2, parts.length));
                                            }

                                            if (success)
                                            {
                                                ++validEntryCounter;
                                            }
                                            else
                                            {
                                                ++invalidEntryCounter;
                                            }
                                        }
                                        else
                                        {
                                            // The case where only the separator is specified in the alignment column
                                            ++invalidEntryCounter;
                                            logger.warning (
                                                "The %s config entry at line %d contains no alignment values, only the separator (\"%s\")",
                                                configName,
                                                i, LOTRServerConstants.FACTION_ALIGNMENT_SEPARATOR);
                                        }

                                    }
                                    catch (NumberFormatException e)
                                    {
                                        ++invalidEntryCounter;
                                        logger.warning (
                                            "The %s config entry at line %d contains invalid alignment values (\"%s\") which aren't numbers",
                                            configName,
                                            i, alignmentString);
                                    }
                                }
                                else
                                {
                                    ++invalidEntryCounter;
                                    logger.warning (
                                        "The %s config entry at line %d contains an invalid alignment string (\"%s\") with too many alignment separators",
                                        configName,
                                        i, alignmentString);
                                }
                            }
                            else
                            {
                                ++invalidEntryCounter;
                                logger.warning (
                                    "The %s config entry at line %d contains no alignment value",
                                    configName,
                                    i);
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
