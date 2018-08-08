package pvpmode.compatibility.modules.lotr;

import java.util.*;

import lotr.common.*;
import net.minecraft.entity.player.EntityPlayer;

public class SafeBiomeOverrideCondition extends MiddleEarthBiomeOverrideCondition
{

    public SafeBiomeOverrideCondition (Map<Integer, Collection<BiomeFactionEntry>> configurationData)
    {
        super (configurationData);
    }

    @Override
    public int getPriority ()
    {
        return 200;
    }

    @Override
    protected Boolean handleCondition (BiomeFactionEntry entry, EntityPlayer player)
    {
        String factionName = entry.getFactionName ();
        if (factionName.equals ("ALL"))
            return Boolean.FALSE;
        else
        {
            LOTRPlayerData data = LOTRLevelData.getData (player);
            if (data.getAlignment (LOTRFaction.forName (factionName)) > entry.getAlignment ())
                return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public String getForcedOverrideMessage (EntityPlayer player, Boolean mode)
    {
        return String.format (
            "PvP is now disabled for %s upon entering a safe biome",
            player.getDisplayName ());
    }

    @Override
    public String getLocalForcedOverrideMessage (EntityPlayer player, Boolean mode)
    {
        return "PvP is now disabled for you upon entering a safe biome";
    }

}
