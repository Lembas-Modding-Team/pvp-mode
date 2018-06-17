package pvpmode.compatibility.modules.lotr;

import java.util.*;

import lotr.common.*;
import lotr.common.world.biome.LOTRBiome;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.biome.BiomeGenBase;
import pvpmode.overrides.PvPOverrideCondition;

/**
 * The override condition for the LOTR biomes. Players which enter a biome
 * assigned to a faction (via the configuration data) are forced to have PvP
 * enabled of they're hostile to this faction.
 * 
 * @author CraftedMods
 *
 */
public class MiddleEarthBiomeOverrideCondition implements PvPOverrideCondition
{

    private Map<Integer, Collection<EnemyBiomeFactionEntry>> configurationData = new HashMap<> ();

    public MiddleEarthBiomeOverrideCondition (Map<Integer, Collection<EnemyBiomeFactionEntry>> configurationData)
    {
        this.configurationData = configurationData;
    }

    @Override
    public int getPriority ()
    {
        return 100;
    }

    @Override
    public Boolean isPvPEnabled (EntityPlayer player)
    {
        Boolean pvpEnabled = null;
        BiomeGenBase currentBiome = player.worldObj.getWorldChunkManager ().getBiomeGenAt ((int) player.posX,
            (int) player.posZ);
        // Check if we are in a relevant LOTR biome
        if (currentBiome instanceof LOTRBiome && configurationData.containsKey (currentBiome.biomeID))
        {
            for (EnemyBiomeFactionEntry entry : configurationData.get (currentBiome.biomeID))
            {
                String factionName = entry.getFactionName ();
                if (factionName.equals ("ALL"))
                {
                    pvpEnabled = Boolean.TRUE;
                }
                else
                {
                    LOTRPlayerData data = LOTRLevelData.getData (player);
                    if (data.getAlignment (LOTRFaction.forName (factionName)) < entry.getMinAlignment ())
                    {
                        pvpEnabled = Boolean.TRUE;
                    }
                }

            }
        }
        return pvpEnabled;
    }

    @Override
    public String getForcedOverrideMessage (EntityPlayer player, Boolean mode)
    {
        return String.format (
            "WARNING: PvP is now enabled for %s because he entered an enemy biome",
            player.getDisplayName ());
    }

}
