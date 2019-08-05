package pvpmode.modules.lotr.api.server;

import java.util.UUID;

import lotr.common.item.LOTRPoisonedDrinks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.server.utils.PvPServerUtils;
import pvpmode.modules.lotr.api.common.LOTRCommonUtils;

public class LOTRServerUtils extends LOTRCommonUtils
{

    private static Provider provider;

    public static boolean setProvider (Provider provider)
    {
        if (LOTRServerUtils.provider == null)
        {
            LOTRServerUtils.provider = provider;
            return true;
        }
        return false;
    }

    public static interface Provider
    {
        public boolean isPoisonBlocked (EntityPlayerMP player, ItemStack itemstack);
    }

}
