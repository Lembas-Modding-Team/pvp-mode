package pvpmode.modules.siegeMode.internal.server;

import net.minecraft.entity.player.EntityPlayer;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.api.server.overrides.PvPOverrideCondition;
import siege.common.siege.SiegeDatabase;

/**
 * An override condition forcing players to having PvP enabled while in sieges.
 *
 * @author CraftedMods
 *
 */
public class SiegeZoneOverrideCondition implements PvPOverrideCondition
{

    @Override
    public int getPriority ()
    {
        return 1000;
    }

    @Override
    public EnumForcedPvPMode getForcedPvPMode (EntityPlayer player)
    {
        return SiegeDatabase.getActiveSiegeForPlayer (player) != null ? EnumForcedPvPMode.ON
            : EnumForcedPvPMode.UNDEFINED;
    }

    @Override
    public String getForcedOverrideMessage (EntityPlayer player, EnumPvPMode forcedMode, boolean global)
    {
        return null;
    }

}
