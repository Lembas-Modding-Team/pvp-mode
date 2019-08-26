package pvpmode.modules.lotr.internal.common.gear;

import java.util.*;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.*;
import lotr.common.fac.LOTRFaction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import pvpmode.api.common.compatibility.events.ArmorItemCheckEvent;
import pvpmode.api.common.compatibility.events.ArmorItemCheckEvent.CheckType;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.modules.lotr.api.common.LOTRCommonConstants;
import pvpmode.modules.lotr.api.common.gear.EnumGearBlockingCondition;
import pvpmode.modules.lotr.internal.common.*;

/**
 * The code of the BlockedGearManager that is executed on both sides.
 *
 * @author CraftedMods
 *
 */
public abstract class CommonBlockedGearManager
{

    protected Map<Item, Map<FactionEntry, EnumGearBlockingCondition>> blockedItems = new HashMap<> ();

    public void init (LOTRModCommonCompatibilityModule module)
    {
        MinecraftForge.EVENT_BUS.register (this);
        FMLCommonHandler.instance ().bus ().register (this);
    }

    public Map<Item, Map<FactionEntry, EnumGearBlockingCondition>> getBlockedItems ()
    {
        return PvPCommonUtils.deepUnmodifiableMap (blockedItems);
    }

    @SubscribeEvent
    public void onArmorItemCheck (ArmorItemCheckEvent event)
    {
        if (areGearItemsBlocked ())
        {
            EntityPlayer player = PvPCommonUtils.getPlayer (event.getEntity ());

            if (!PvPCommonUtils.isCreativeMode (player))
            {
                if (!canPlayerUseItem (player, event.getArmorItem ().getItem ()))
                {
                    if (event.getCheckType () == CheckType.EQUIP
                        && isEquippingOfBlockedArmorBlocked ()
                        || event.getCheckType () == CheckType.PROTECT)
                    {
                        event.setCanceled (true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemRightClick (PlayerUseItemEvent.Start event)
    {
        if (areGearItemsBlocked () && !PvPCommonUtils.isCreativeMode (event.entityPlayer))
        {
            ItemStack currentStack = event.item;
            if (currentStack != null) // Shouldn't be necessary, but one never knows...
                if (!canPlayerUseItem (event.entityPlayer, currentStack.getItem ()))
                {
                    event.setCanceled (true);
                    onItemUsageCanceled (event.entityPlayer, currentStack);
                }
        }
    }

    /**
     * Checks whether the specified player can use the specified item. The condition
     * will be determined by the current state of the player.
     *
     * @param player
     *            The player to check
     * @param item
     *            The item to check
     * @return Whether the item can be used by the specified player
     */
    public boolean canPlayerUseItem (EntityPlayer player, Item item)
    {
        return canPlayerUseItem (player, item,
            PvPCommonUtils.isInPvP (player) ? EnumGearBlockingCondition.PVP : EnumGearBlockingCondition.PVE);
    }

    /**
     * Checks whether the specified player can use the specified item under the
     * specified condition.
     *
     * @param player
     *            The player to check
     * @param item
     *            The item to check
     * @param condition
     *            The condition to check
     * @return Whether the item can be used by the specified player under the
     *         specified condition
     */
    public boolean canPlayerUseItem (EntityPlayer player, Item item, EnumGearBlockingCondition condition)
    {
        boolean isUseableInPvP = true;
        boolean isUseableInPvE = true;

        if (blockedItems.containsKey (item))
        {
            // Now check whether the player is allowed to use the item
            Map<FactionEntry, EnumGearBlockingCondition> factionData = blockedItems.get (item);

            /*
             * A condition is fulfilled, if a player has the minimum required alignment with
             * the faction. Also, the current PvP/PvE state of the player is taken into
             * account. If there are no conditions for the PvP/PvE state condition, then the
             * item can be used. However, if there are some, at least one of them must be
             * fulfilled.
             * 
             */
            for (FactionEntry entry : factionData.keySet ())
            {
                EnumGearBlockingCondition entryCondition = factionData.get (entry);
                switch (entryCondition)
                {
                    case ALWAYS:
                        isUseableInPvE = false;
                        isUseableInPvP = false;
                        break;
                    case PVE:
                        isUseableInPvE = false;
                        break;
                    case PVP:
                        isUseableInPvP = false;
                        break;
                }

                LOTRPlayerData data = LOTRLevelData.getData (player);

                boolean stopIterating = false;

                for (String factionName : entry.getInvolvedFactions ())
                {
                    if (!factionName.equals (LOTRCommonConstants.FACTION_ENTRY_WILDCARD))
                    {
                        LOTRFaction faction = LOTRFaction.forName (factionName);
                        if (data.getAlignment (faction) >= entry
                            .getAlignment () && (entry.isPledgingRequired () ? data.isPledgedTo (faction) : true))
                        {
                            switch (entryCondition)
                            {
                                case ALWAYS:
                                    isUseableInPvE = true;
                                    isUseableInPvP = true;
                                    break;
                                case PVE:
                                    isUseableInPvE = true;
                                    break;
                                case PVP:
                                    isUseableInPvP = true;
                                    break;
                            }
                            if (condition == EnumGearBlockingCondition.ALWAYS ? isUseableInPvP && isUseableInPvE
                                : condition == EnumGearBlockingCondition.PVP ? isUseableInPvP : isUseableInPvE)
                            {
                                stopIterating = true;
                                break;
                            }
                        }
                    }
                }
                if (stopIterating)
                    break;
            }
        }
        return condition == EnumGearBlockingCondition.ALWAYS ? isUseableInPvP && isUseableInPvE
            : condition == EnumGearBlockingCondition.PVP ? isUseableInPvP
                : isUseableInPvE;
    }

    protected void onItemUsageCanceled (EntityPlayer player, ItemStack stack)
    {

    }

    protected abstract boolean areGearItemsBlocked ();

    protected abstract boolean isEquippingOfBlockedArmorBlocked ();

}
