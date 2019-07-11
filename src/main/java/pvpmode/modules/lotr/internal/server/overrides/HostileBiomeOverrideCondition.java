package pvpmode.modules.lotr.internal.server.overrides;

import java.util.*;

import lotr.common.*;
import lotr.common.fac.LOTRFaction;
import net.minecraft.entity.player.EntityPlayer;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.modules.lotr.api.server.LOTRServerConstants;
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
        if (entry.getEntryName ().equals (LOTRServerConstants.FACTION_ENTRY_WILDCARD))
            return EnumForcedPvPMode.ON;
        else
        {
            LOTRPlayerData data = LOTRLevelData.getData (player);
            if (entry.getInvolvedFactions ().stream ().allMatch (
                factionName -> data.getAlignment (LOTRFaction.forName (factionName)) < entry
                    .getAlignment ()))
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
