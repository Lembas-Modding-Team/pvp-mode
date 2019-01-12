package pvpmode.modules.lotr.internal.server.overrides;

import java.util.*;

import lotr.common.*;
import net.minecraft.entity.player.EntityPlayer;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.modules.lotr.internal.server.FactionEntry;

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

    public HostileBiomeOverrideCondition (Map<Integer, Collection<FactionEntry>> configurationData)
    {
        super (configurationData);
    }

    @Override
    public int getPriority ()
    {
        return 100;
    }

    @Override
    protected EnumForcedPvPMode handleCondition (FactionEntry entry, EntityPlayer player)
    {
        String factionName = entry.getFactionName ();
        if (factionName.equals ("ALL"))
            return EnumForcedPvPMode.ON;
        else
        {
            LOTRPlayerData data = LOTRLevelData.getData (player);
            if (data.getAlignment (LOTRFaction.forName (factionName)) < entry.getAlignment ())
                return EnumForcedPvPMode.ON;
        }
        return EnumForcedPvPMode.UNDEFINED;
    }

    @Override
    public String getForcedOverrideMessage (EntityPlayer player, EnumPvPMode forcedMode, boolean global)
    {
        return String.format (
            "PvP is now enabled for %s upon entering an enemy biome",
            global ? player.getDisplayName () : "you");
    }

}
