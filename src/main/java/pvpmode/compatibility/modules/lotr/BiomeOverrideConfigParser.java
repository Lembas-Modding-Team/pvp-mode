package pvpmode.compatibility.modules.lotr;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import cpw.mods.fml.common.FMLLog;
import lotr.common.fac.LOTRFaction;

public class BiomeOverrideConfigParser
{

    private final String configName;
    private final Path file;

    public BiomeOverrideConfigParser (String configName, Path file)
    {
        this.configName = configName;
        this.file = file;
    }

    public Map<Integer, Collection<BiomeFactionEntry>> parse () throws IOException
    {
        Map<Integer, Collection<BiomeFactionEntry>> configurationData = new HashMap<> ();

        // Parse and load the config file
        int validEntryCounter = 0;
        int invalidEntryCounter = 0;
        Set<String> readValidFactionEntries = new HashSet<> ();
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
                    if (parts.length != 3)
                    {
                        // There are more or less than three columns
                        ++invalidEntryCounter;
                        FMLLog.warning (
                            "The %s config entry \"%s\" at line %d is invalid. There're too much or too less columns separated by semicolons!",
                            configName,
                            line,
                            i);
                    }
                    else
                    {
                        // Extract the faction identifier from the first column
                        String faction = parts[0].trim ();
                        if (faction.equals ("ALL") || LOTRFaction.forName (faction) != null)
                        {
                            if (readValidFactionEntries.contains (faction))
                            {
                                // The faction was specified already
                                ++invalidEntryCounter;
                                FMLLog.warning (
                                    "The %s config entry at line %d references a faction (\"%s\") which was referenced by an entry loaded before. It'll be ignored.",
                                    configName, i, faction);
                            }
                            else
                            {
                                String alignmentString = parts[1].trim ();
                                try
                                {
                                    // Extract the minimum alignment from the second column
                                    Integer alignmentInt = Integer.parseInt (alignmentString);

                                    // Extract the biome ids from the first column
                                    String[] biomeIds = parts[2].trim ().split (",");
                                    if (biomeIds.length <= 0)
                                    {
                                        // No biomes were specified
                                        ++invalidEntryCounter;
                                        FMLLog.warning (
                                            "The %s config entry at line %d contains no assigned biome ids", configName,
                                            i);
                                    }
                                    else
                                    {
                                        // Parse the biome ids
                                        Collection<Integer> biomeIdsInt = new HashSet<> ();
                                        for (String biomeString : biomeIds)
                                        {
                                            String biomeStringTrimmed = biomeString.trim ();
                                            try
                                            {
                                                if (!biomeIdsInt.add (Integer.parseInt (biomeStringTrimmed)))
                                                {
                                                    // Duplicated biome ids specified
                                                    FMLLog.warning (
                                                        "The %s config entry at line %d contains a duplicated biome id (%s).",
                                                        configName,
                                                        i, biomeStringTrimmed);
                                                }
                                            }
                                            catch (NumberFormatException e)
                                            {
                                                FMLLog.warning (
                                                    "The %s config entry at line %d contains an invalid biome id (\"%s\"). The invalid id will be ignored.",
                                                    configName,
                                                    i, biomeStringTrimmed);
                                            }
                                        }
                                        if (biomeIdsInt.isEmpty ())
                                        {
                                            // Biome ids were specified, but all were invalid
                                            ++invalidEntryCounter;
                                            FMLLog.warning (
                                                "The %s config entry at line %d contains only invalid biome ids. The entry will be ignored.",
                                                configName,
                                                i);
                                        }
                                        else
                                        {
                                            // Add the data to our data structured
                                            readValidFactionEntries.add (faction);
                                            BiomeFactionEntry entry = new BiomeFactionEntry (faction,
                                                alignmentInt);
                                            for (Integer biomeId : biomeIdsInt)
                                            {
                                                if (!configurationData.containsKey (biomeId))
                                                {
                                                    configurationData.put (biomeId, new HashSet<> ());
                                                }
                                                configurationData.get (biomeId).add (entry);
                                            }
                                            ++validEntryCounter;
                                        }
                                    }
                                }
                                catch (NumberFormatException e)
                                {
                                    ++invalidEntryCounter;
                                    FMLLog.warning (
                                        "The %s config entry at line %d contains an invalid minimum alignment (\"%s\")",
                                        configName,
                                        i, alignmentString);
                                }
                            }
                        }
                        else
                        {
                            // The faction name is invalid
                            ++invalidEntryCounter;
                            FMLLog.warning (
                                "The %s config entry at line %d contains an invalid faction name (\"%s\").", configName,
                                i, faction);
                        }
                    }
                }
            }
        }

        FMLLog.info ("Loaded %d of %d specified %s config entries. %d config entries are invalid.",
            validEntryCounter, invalidEntryCounter + validEntryCounter, configName,
            invalidEntryCounter);

        return configurationData;
    }

}
