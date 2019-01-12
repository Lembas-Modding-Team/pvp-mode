package pvpmode.modules.lotr.internal.server.overrides;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import pvpmode.api.common.SimpleLogger;
import pvpmode.modules.lotr.internal.server.*;

public class BiomeOverrideConfigParser extends FactionEntryParser
{

    private final Map<Integer, Collection<FactionEntry>> parsedData = new HashMap<> ();

    public BiomeOverrideConfigParser (String configName, Path file, SimpleLogger logger)
    {
        super (configName, file, logger);
    }

    @Override
    protected boolean parseLine (FactionEntry entry, int line, String[] remainingParts)
    {
        Collection<Integer> biomeIds = this.parseList ("biome ID", remainingParts[0], line, id ->
        {
            try
            {
                return Integer.parseInt (id);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        });

        if (biomeIds == null)
            return false;

        // Add the data to our data structures
        for (Integer biomeId : biomeIds)
        {
            if (!parsedData.containsKey (biomeId))
            {
                parsedData.put (biomeId, new HashSet<> ());
            }
            parsedData.get (biomeId).add (entry);
        }
        return true;
    }

    public void parse () throws IOException
    {
        parsedData.clear ();
        super.parse ();
    }

    public Map<Integer, Collection<FactionEntry>> getParsedData ()
    {
        return parsedData;
    }

}
