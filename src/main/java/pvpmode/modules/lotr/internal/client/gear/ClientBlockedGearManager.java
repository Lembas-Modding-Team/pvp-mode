package pvpmode.modules.lotr.internal.client.gear;

import java.util.Map;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.item.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.modules.lotr.api.common.gear.EnumGearBlockingCondition;
import pvpmode.modules.lotr.internal.common.FactionEntry;
import pvpmode.modules.lotr.internal.common.gear.CommonBlockedGearManager;

public class ClientBlockedGearManager extends CommonBlockedGearManager
{

    private boolean areGearItemsBlockedServerside;
    private boolean isEquippingOfBlockedArmorBlocked;

    @Override
    protected boolean areGearItemsBlocked ()
    {
        return areGearItemsBlockedServerside;
    }

    public void setAreGearItemsBlockedServerside (boolean areGearItemsBlockedServerside)
    {
        this.areGearItemsBlockedServerside = areGearItemsBlockedServerside;
    }

    @Override
    protected boolean isEquippingOfBlockedArmorBlocked ()
    {
        return isEquippingOfBlockedArmorBlocked;
    }

    public void setEquippingOfBlockedArmorBlocked (boolean isEquippingOfBlockedArmorBlocked)
    {
        this.isEquippingOfBlockedArmorBlocked = isEquippingOfBlockedArmorBlocked;
    }

    public void setBlockedItems (Map<Item, Map<FactionEntry, EnumGearBlockingCondition>> blockedItems)
    {
        this.blockedItems.clear ();
        this.blockedItems.putAll (blockedItems);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip (ItemTooltipEvent event)
    {
        if (areGearItemsBlocked () &&
            !PvPCommonUtils.isCreativeMode (event.entityPlayer))
        {
            ItemStack currentStack = event.itemStack;
            if (currentStack != null) // Shouldn't be necessary, but one never knows...
            {
                boolean blockedInPvP = !this.canPlayerUseItem (event.entityPlayer, currentStack.getItem (),
                    EnumGearBlockingCondition.PVP);
                boolean blockedInPvE = !this.canPlayerUseItem (event.entityPlayer, currentStack.getItem (),
                    EnumGearBlockingCondition.PVE);

                boolean blockedInPvPOnly = blockedInPvP && !blockedInPvE;
                boolean blockedInPvEOnly = !blockedInPvP && blockedInPvE;

                if (blockedInPvP || blockedInPvE)
                {
                    event.toolTip.add (1, "");
                }

                if (!this.canPlayerUseItem (event.entityPlayer,
                    currentStack.getItem ()))
                {
                    if (!blockedInPvE)
                    {
                        event.toolTip.add (1,
                            String.format ("%s(Can be used in PvE)", EnumChatFormatting.GREEN));
                    }
                    else if (!blockedInPvP)
                    {
                        event.toolTip.add (1,
                            String.format ("%s(Can be used in PvP)", EnumChatFormatting.GREEN));
                    }
                    event.toolTip.add (1,
                        String.format (" - %s%sItem usage is blocked", EnumChatFormatting.DARK_RED,
                            EnumChatFormatting.BOLD));
                    if (currentStack.getItem () instanceof ItemArmor)
                    {
                        event.toolTip.add (1,
                            String.format (" - %s%sNo protection", EnumChatFormatting.DARK_RED,
                                EnumChatFormatting.BOLD));
                    }
                    event.toolTip.add (1,
                        String.format ("%s%sCompletely useless now", EnumChatFormatting.DARK_RED,
                            EnumChatFormatting.BOLD));
                }
                else if (blockedInPvPOnly)
                {
                    event.toolTip.add (1,
                        String.format (" - %sItem usage is blocked in PvP", EnumChatFormatting.YELLOW));
                    if (currentStack.getItem () instanceof ItemArmor)
                    {
                        event.toolTip.add (1,
                            String.format (" - %sNo protection in PvP", EnumChatFormatting.YELLOW));
                    }
                    event.toolTip.add (1, String.format ("%sCompletely useless in PvP", EnumChatFormatting.YELLOW));
                }
                else if (blockedInPvEOnly)
                {
                    event.toolTip.add (1,
                        String.format (" - %sItem usage is blocked in PvE", EnumChatFormatting.YELLOW));
                    if (currentStack.getItem () instanceof ItemArmor)
                    {
                        event.toolTip.add (1,
                            String.format (" - %sNo protection in PvE", EnumChatFormatting.YELLOW));
                    }
                    event.toolTip.add (1, String.format ("%sCompletely useless in PvE", EnumChatFormatting.YELLOW));
                }

            }
        }

    }

}
