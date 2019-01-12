package pvpmode.modules.lotr.internal.server.overrides;

import java.util.*;

import lotr.common.*;
import net.minecraft.entity.player.EntityPlayer;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.modules.lotr.internal.server.FactionEntry;

public class SafeBiomeOverrideCondition extends MiddleEarthBiomeOverrideCondition
{

    public SafeBiomeOverrideCondition (Map<Integer, Collection<FactionEntry>> configurationData)
    {
        super (configurationData);
    }

    @Override
    public int getPriority ()
    {
        return 200;
    }

    @Override
    protected EnumForcedPvPMode handleCondition (FactionEntry entry, EntityPlayer player)
    {
        String factionName = entry.getFactionName ();
        if (factionName.equals ("ALL"))
            return EnumForcedPvPMode.OFF;
        else
        {
            LOTRPlayerData data = LOTRLevelData.getData (player);
            if (data.getAlignment (LOTRFaction.forName (factionName)) > entry.getAlignment ())
                return EnumForcedPvPMode.OFF;
        }
        return EnumForcedPvPMode.UNDEFINED;
    }

    @Override
    public String getForcedOverrideMessage (EntityPlayer player, EnumPvPMode forcedMode, boolean global)
    {
        return String.format (
            "PvP is now disabled for %s upon entering a safe biome",
            global ? player.getDisplayName () : "you");
    }

}
