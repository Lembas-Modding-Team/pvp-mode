package pvpmode.modules.lotr.internal.server.gear;

import java.io.IOException;
import java.util.*;

import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import pvpmode.PvPMode;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.utils.*;
import pvpmode.internal.server.ServerProxy;
import pvpmode.modules.lotr.api.common.LOTRCommonConstants;
import pvpmode.modules.lotr.api.server.*;
import pvpmode.modules.lotr.internal.common.gear.CommonBlockedGearManager;
import pvpmode.modules.lotr.internal.common.network.BlockedGearItemsListChangedMessage;
import pvpmode.modules.lotr.internal.server.LOTRModServerCompatibilityModule;

public class ServerBlockedGearManager extends CommonBlockedGearManager
{

    private LOTRModServerCompatibilityModule module;

    // private final Random random = new Random ();

    private Map<UUID, Long> lastWarningDisplay = new HashMap<> ();

    public ServerBlockedGearManager ()
    {

    }

    public void init (LOTRModServerCompatibilityModule module, LOTRServerConfiguration config)
    {
        this.module = module;

        module.recreateFile (module.getConfigurationFolder (), "default_blocked_gear.txt",
            LOTRServerConstants.BLOCKED_GEAR_CONFIGURATION_FILE_NAME,
            "blocked gear configuration file", true);

        BlockedGearConfigParser parser = new BlockedGearConfigParser ("blocked gear",
            module.getConfigurationFolder ().resolve (LOTRServerConstants.BLOCKED_GEAR_CONFIGURATION_FILE_NAME),
            module.getLogger (), config);

        try
        {
            parser.parse ();
            blockedItems.clear ();
            blockedItems.putAll (parser.getBlockedItems ());

            ServerProxy server = PvPMode.instance.getServerProxy ();

            PvPServerUtils.getClientsWithCompatibilityModule (LOTRCommonConstants.LOTR_MOD_MODID).forEach (clientData ->
            {
                server.getPacketDispatcher ().sendTo (new BlockedGearItemsListChangedMessage (blockedItems),
                    clientData.getPlayer ());
            });

        }
        catch (IOException e)
        {
            module.getLogger ().errorThrowable ("Couldn't parse the blocked gear configuration", e);
        }

        super.init (module);

    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void interceptPvP (LivingAttackEvent event)
    {
        if (module.getConfiguration ().areGearItemsBlocked ())
        {
            // Check whether we have a *real* player
            EntityPlayerMP player = (EntityPlayerMP) PvPServerUtils.getPlayer (event.source.getEntity ());
            if (player != null && !PvPServerUtils.isCreativeMode (player))
            {
                ItemStack equippedStack = player.getCurrentEquippedItem ();
                if (equippedStack != null)
                {
                    if (!canPlayerUseItem (player, equippedStack.getItem ()))
                    {
                        event.setCanceled (true);
                        this.displayItemBlockedMessage (player);
                    }
                }
            }
        }
    }

    @Override
    protected void onItemUsageCanceled (EntityPlayer player, ItemStack stack)
    {
        this.displayItemBlockedMessage (player);
    }

    @Override
    protected boolean areGearItemsBlocked ()
    {
        return module.getConfiguration ().areGearItemsBlocked ();
    }

    @Override
    protected boolean isEquippingOfBlockedArmorBlocked ()
    {
        return module.getConfiguration ().isEquippingOfBlockedArmorBlocked ();
    }

    private Map<UUID, Long> lastArmorInventoryCheck = new HashMap<> ();
    private Map<UUID, Long> futureArmorItemRemoveCheck = new HashMap<> ();

    @SubscribeEvent
    public void onPlayerTick (PlayerTickEvent event)
    {
        if (module.getConfiguration ().areGearItemsBlocked ())
        {
            EntityPlayer player = event.player;

            if (!PvPServerUtils.isCreativeMode (player))
            {

                int armorInventoryCheckInterval = module.getConfiguration ().getArmorInventoryCheckInterval ();
                int blockedArmorRemoveTimer = module.getConfiguration ().getBlockedArmorRemoveTimer ();

                if (armorInventoryCheckInterval != -1)
                {

                    UUID playerUUID = player.getUniqueID ();

                    if (!lastArmorInventoryCheck.containsKey (playerUUID))
                        lastArmorInventoryCheck.put (playerUUID,
                            PvPServerUtils.getTime () + armorInventoryCheckInterval);

                    if ( (PvPServerUtils.getTime ()
                        - lastArmorInventoryCheck.get (playerUUID)) > armorInventoryCheckInterval)
                    {
                        int count = 0;

                        for (ItemStack stack : player.inventory.armorInventory)
                        {
                            if (stack != null)
                            {
                                if (!canPlayerUseItem (player, stack.getItem ()))
                                {
                                    ++count;
                                }
                            }
                        }

                        if (count > 0)
                        {
                            // A check could be executed while the timer is already running, in that case,
                            // don't do anything
                            if (blockedArmorRemoveTimer != -1 && !futureArmorItemRemoveCheck.containsKey (playerUUID))
                            {
                                futureArmorItemRemoveCheck.put (playerUUID,
                                    PvPServerUtils.getTime () + blockedArmorRemoveTimer);
                            }
                            long removalTimer = futureArmorItemRemoveCheck.getOrDefault (playerUUID, -1l)
                                - PvPServerUtils.getTime ();
                            ChatComponentText warningMessage1 = new ChatComponentText ("You wear ");
                            ChatComponentText warningMessage2 = new ChatComponentText (Integer.toString (count));
                            ChatComponentText warningMessage3 = new ChatComponentText (
                                " blocked armor items, which won't protect you.");

                            warningMessage2.getChatStyle ().setColor (EnumChatFormatting.DARK_RED);

                            if (removalTimer >= 0)
                            {
                                ChatComponentText warningMessage4 = new ChatComponentText (
                                    " They'll be unequipped in ");
                                ChatComponentText warningMessage5 = new ChatComponentText (
                                    Long.toString (removalTimer));
                                ChatComponentText warningMessage6 = new ChatComponentText (
                                    " seconds.");

                                warningMessage5.getChatStyle ().setColor (EnumChatFormatting.DARK_RED);

                                warningMessage3.appendSibling (warningMessage4).appendSibling (warningMessage5)
                                    .appendSibling (warningMessage6);
                            }

                            warningMessage1.getChatStyle ().setColor (EnumChatFormatting.RED);

                            player.addChatComponentMessage (
                                warningMessage1.appendSibling (warningMessage2).appendSibling (warningMessage3));
                        }

                        lastArmorInventoryCheck.put (playerUUID, PvPServerUtils.getTime ());
                    }

                    if (blockedArmorRemoveTimer != -1 && futureArmorItemRemoveCheck.containsKey (playerUUID))
                    {
                        if (PvPServerUtils.getTime () >= futureArmorItemRemoveCheck.get (playerUUID))
                        {
                            List<ItemStack> removedItems = new ArrayList<> ();

                            for (int i = 0; i < player.inventory.armorInventory.length; i++)
                            {
                                ItemStack stack = player.inventory.armorInventory[i];
                                if (stack != null)
                                {
                                    if (!canPlayerUseItem (player, stack.getItem ()))
                                    {
                                        removedItems.add (stack);
                                        player.inventory.armorInventory[i] = null;
                                    }
                                }
                            }

                            if (!removedItems.isEmpty ())
                            {

                                int itemsStoredInInventory = 0;

                                // Try to store the items in inventory
                                for (int i = 0; (i < player.inventory.mainInventory.length)
                                    && !removedItems.isEmpty (); i++)
                                {
                                    if (player.inventory.mainInventory[i] == null)
                                    {
                                        // Found an empty slot - fill it
                                        player.inventory.mainInventory[i] = removedItems.get (0);
                                        removedItems.remove (0);
                                        ++itemsStoredInInventory;
                                    }
                                }

                                int armorRemoveAction = module.getConfiguration ().getArmorRemoveAction ();

                                if (!removedItems.isEmpty ())
                                {
                                    switch (armorRemoveAction)
                                    {
                                        case 0:
                                            removedItems.forEach (stack -> player.entityDropItem (stack, 0.5f));
                                            break;
                                        case 1:
                                            PvPData pvpData = PvPServerUtils.getPvPData (player);
                                            List<ItemStack> vault = pvpData.getVault ();
                                            vault.addAll (removedItems);
                                            pvpData.setVault (vault);
                                            break;
                                    }

                                }

                                ChatComponentText removeInformation1 = new ChatComponentText ("");

                                if (itemsStoredInInventory > 0)
                                {
                                    ChatComponentText removeInformation2 = new ChatComponentText (
                                        Integer.toString (itemsStoredInInventory));
                                    ChatComponentText removeInformation3 = new ChatComponentText (
                                        " items from your armor inventory were moved to your main inventory");

                                    removeInformation2.getChatStyle ().setColor (EnumChatFormatting.DARK_RED);

                                    removeInformation1.appendSibling (removeInformation2)
                                        .appendSibling (removeInformation3);
                                }

                                if (itemsStoredInInventory > 0 && removedItems.size () > 0)
                                {
                                    ChatComponentText removeInformation4 = new ChatComponentText (" and ");

                                    removeInformation1.appendSibling (removeInformation4);
                                }

                                if (removedItems.size () > 0)
                                {
                                    ChatComponentText removeInformation5 = new ChatComponentText (
                                        Integer.toString (removedItems.size ()));
                                    ChatComponentText removeInformation6 = new ChatComponentText (
                                        String.format (" items from your armor inventory were %s",
                                            armorRemoveAction == 0 ? "dropped to the ground" : "stored in your vault"));

                                    removeInformation5.getChatStyle ().setColor (EnumChatFormatting.DARK_RED);

                                    removeInformation1.appendSibling (removeInformation5)
                                        .appendSibling (removeInformation6);
                                }

                                removeInformation1.getChatStyle ().setColor (EnumChatFormatting.RED);

                                player.addChatComponentMessage (removeInformation1);

                            }

                            futureArmorItemRemoveCheck.remove (playerUUID);
                        }
                    }
                }

            }
        }

    }

    private void displayItemBlockedMessage (EntityPlayer player)
    {
        int cooldown = module.getConfiguration ()
            .getItemUsageBlockedMessageCooldown ();

        if (cooldown != -1)
        {
            UUID playerUUID = player.getUniqueID ();
            if (!lastWarningDisplay.containsKey (playerUUID))
                lastWarningDisplay.put (playerUUID, PvPServerUtils.getTime () - cooldown - 1);

            if ( (PvPServerUtils.getTime () - lastWarningDisplay.get (playerUUID)) > cooldown)
            {
                ServerChatUtils.red (player, "The usage of this item is blocked for you.");
                lastWarningDisplay.put (playerUUID, PvPServerUtils.getTime ());
            }
        }
    }

}
