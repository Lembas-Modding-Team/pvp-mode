package pvpmode.compatibility.modules.siegeMode;

import net.minecraft.entity.player.EntityPlayer;
import pvpmode.overrides.PvPOverrideCondition;
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
    public Boolean isPvPEnabled (EntityPlayer player)
    {
        return SiegeDatabase.getActiveSiegeForPlayer (player) != null ? Boolean.TRUE : null;
    }

    @Override
    public String getForcedOverrideMessage (EntityPlayer player, Boolean mode)
    {
        return null;
    }

}
