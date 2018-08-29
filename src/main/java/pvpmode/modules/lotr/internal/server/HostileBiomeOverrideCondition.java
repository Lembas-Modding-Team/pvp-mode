package pvpmode.modules.lotr.internal.server;

import java.util.*;

import lotr.common.*;
import net.minecraft.entity.player.EntityPlayer;

/**
 * The override condition for the LOTR biomes. Players which enter a biome
 * assigned to a faction (via the configuration data) are forced to have PvP
 * enabled if they're hostile to this faction.
 *
 * @author CraftedMods
 *
 */
public class HostileBiomeOverrideCondition extends MiddleEarthBiomeOverrideCondition
{

    public HostileBiomeOverrideCondition (Map<Integer, Collection<BiomeFactionEntry>> configurationData)
    {
        super (configurationData);
    }

    @Override
    public int getPriority ()
    {
        return 100;
    }

    @Override
    protected Boolean handleCondition (BiomeFactionEntry entry, EntityPlayer player)
    {
        String factionName = entry.getFactionName ();
        if (factionName.equals ("ALL"))
            return Boolean.TRUE;
        else
        {
            LOTRPlayerData data = LOTRLevelData.getData (player);
            if (data.getAlignment (LOTRFaction.forName (factionName)) < entry.getAlignment ())
                return Boolean.TRUE;
        }
        return null;
    }

    @Override
    public String getForcedOverrideMessage (EntityPlayer player, Boolean mode)
    {
        return String.format (
            "PvP is now enabled for %s upon entering an enemy biome",
            player.getDisplayName ());
    }

    @Override
    public String getLocalForcedOverrideMessage (EntityPlayer player, Boolean mode)
    {
        return "PvP is now enabled for you upon entering an enemy biome";
    }

}
