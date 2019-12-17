package pvpmode.modules.lotr.internal.server.gear;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import net.minecraft.item.Item;
import pvpmode.api.common.SimpleLogger;
import pvpmode.modules.lotr.api.common.gear.EnumGearBlockingCondition;
import pvpmode.modules.lotr.api.server.LOTRServerConfiguration;
import pvpmode.modules.lotr.internal.common.FactionEntry;
import pvpmode.modules.lotr.internal.server.*;

public class BlockedGearConfigParser extends FactionEntryParser
{

    private Map<Item, Map<FactionEntry, EnumGearBlockingCondition>> blockedItems = new HashMap<> ();

    public BlockedGearConfigParser (String configName, Path file, SimpleLogger logger, LOTRServerConfiguration config)
    {
        super (configName, file, logger, config);
    }

    @Override
    public void parse () throws IOException
    {
        blockedItems.clear ();
        super.parse ();
    }

    @Override
    protected boolean parseLine (FactionEntry entry, int line, String[] remainingParts)
    {
        EnumGearBlockingCondition condition = EnumGearBlockingCondition.ALWAYS;
        if (remainingParts.length > 1)
        {
            // Means that a condition is specified
            try
            {
                condition = EnumGearBlockingCondition.valueOf (remainingParts[0].trim ());
            }
            catch (IllegalArgumentException e)
            {
                logger.warning (
                    "The %s config entry at line %d contains an invalid blocking condition (\"%s\")",
                    configName,
                    line, remainingParts[0]);
                return false;
            }
        }

        Collection<Item> items = this.parseList ("item", remainingParts[remainingParts.length > 1 ? 1 : 0], line,
            name ->
            {
                Object item = Item.itemRegistry.getObject (name);
                if (item instanceof Item)
                    return (Item) item;
                return null;
            });

        if (items == null)
            return false;

        // Add the data to our data structures
        for (Item item : items)
        {
            if (!blockedItems.containsKey (item))
                blockedItems.put (item, new HashMap<> ());
            blockedItems.get (item).put (entry, condition);
        }

        return true;
    }

    public Map<Item, Map<FactionEntry, EnumGearBlockingCondition>> getBlockedItems ()
    {
        return blockedItems;
    }

}
