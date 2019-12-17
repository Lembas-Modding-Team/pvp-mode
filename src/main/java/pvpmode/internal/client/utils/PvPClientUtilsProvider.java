package pvpmode.internal.client.utils;

import net.minecraft.entity.player.EntityPlayer;
import pvpmode.PvPMode;
import pvpmode.api.client.utils.PvPClientUtils;

public class PvPClientUtilsProvider implements PvPClientUtils.Provider
{

    @Override
    public boolean isInPvP (EntityPlayer player)
    {
        return PvPMode.instance.getClientProxy ().isInPvP (player);
    }

}
