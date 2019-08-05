package pvpmode.modules.lotr.internal.server;

import java.util.UUID;

import lotr.common.item.LOTRPoisonedDrinks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.server.utils.PvPServerUtils;
import pvpmode.modules.lotr.api.server.LOTRServerConfiguration;
import pvpmode.modules.lotr.api.server.LOTRServerUtils.Provider;

public class LOTRServerUtilsProvider implements Provider
{

    public LOTRServerUtilsProvider (LOTRModCompatibilityModule moduleIn, LOTRServerConfiguration configIn)
    {
        module = moduleIn;
        config = configIn;
    }

    private final LOTRModCompatibilityModule module;
    private final LOTRServerConfiguration config;

    @Override
    public boolean isPoisonBlocked (EntityPlayerMP player, ItemStack itemstack)
    {
        boolean poisoned = LOTRPoisonedDrinks.isDrinkPoisoned (itemstack);

        if (!poisoned)
            return false;

        int mode = config.isPoisonedDrinksMode ();

        if (mode == 0)
            return false;
        else if (mode == 2)
            return true;

        UUID poisonerID = LOTRPoisonedDrinks.getPoisonerUUID (itemstack);
        UUID victimID = player.getUniqueID ();

        if(!config.isPoisonedDrinksCanPoisonOwner ())
            if(poisonerID != null && victimID.equals (poisonerID))
                return true;

        EnumPvPMode victimMode = PvPServerUtils.getPvPMode (player);
        if (victimMode != EnumPvPMode.ON)
        {
            return true;
        }
        else
        {
            EntityPlayerMP poisoner = PvPServerUtils.getPlayer (poisonerID);
            if (poisoner != null)
            {
                EnumPvPMode poisonerMode = PvPServerUtils.getPvPMode (player);
                if (poisonerMode != EnumPvPMode.ON)
                {
                    return true;
                }
            }
        }

        return false;
    }

}
