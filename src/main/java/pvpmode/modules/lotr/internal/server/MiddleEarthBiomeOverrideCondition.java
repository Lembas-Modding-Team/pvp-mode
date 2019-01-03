package pvpmode.modules.lotr.internal.server;

import java.util.*;

import lotr.common.world.biome.LOTRBiome;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.biome.BiomeGenBase;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.api.server.overrides.PvPOverrideCondition;

/**
 * The basic override condition for the LOTR biomes.
 *
 * @author CraftedMods
 *
 */
public abstract class MiddleEarthBiomeOverrideCondition implements PvPOverrideCondition
{

    private Map<Integer, Collection<BiomeFactionEntry>> configurationData = new HashMap<> ();

    public MiddleEarthBiomeOverrideCondition (Map<Integer, Collection<BiomeFactionEntry>> configurationData)
    {
        this.configurationData = configurationData;
    }

    @Override
    public EnumForcedPvPMode getForcedPvPMode (EntityPlayer player)
    {
        EnumForcedPvPMode forcedPvPMode = EnumForcedPvPMode.UNDEFINED;
        BiomeGenBase currentBiome = player.worldObj.getWorldChunkManager ().getBiomeGenAt ((int) player.posX,
            (int) player.posZ);
        // Check if we are in a relevant LOTR biome
        if (currentBiome instanceof LOTRBiome && configurationData.containsKey (currentBiome.biomeID))
        {
            for (BiomeFactionEntry entry : configurationData.get (currentBiome.biomeID))
            {
                EnumForcedPvPMode enabled = handleCondition (entry, player);
                if (enabled != EnumForcedPvPMode.UNDEFINED)
                {
                    forcedPvPMode = enabled;
                }

            }
        }
        return forcedPvPMode;
    }

    protected abstract EnumForcedPvPMode handleCondition (BiomeFactionEntry entry, EntityPlayer player);

}
