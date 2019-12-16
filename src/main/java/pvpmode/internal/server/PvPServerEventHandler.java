package pvpmode.internal.server;

import java.util.*;
import java.util.function.Predicate;

import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.gameevent.PlayerEvent.*;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.*;
import net.minecraft.event.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import pvpmode.PvPMode;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.api.common.version.*;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.compatibility.events.*;
import pvpmode.api.server.compatibility.events.PartialItemDropEvent.Drop.Action;
import pvpmode.api.server.compatibility.events.PartialItemDropEvent.EnumInventory;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.utils.*;

public class PvPServerEventHandler
{
    private final ServerProxy server;
    private final ServerConfiguration config;

    private final Random random = new Random ();

    public PvPServerEventHandler ()
    {
        server = PvPMode.instance.getServerProxy ();
        config = server.getConfiguration ();
    }

    /**
     * Cancels combat events associated with PvP-disabled players. Note that
     * this function will be invoked twice per attack - this is because of a
     * Forge bug, but the {@link PvPCommonUtils#isCurrentAttackDuplicate} call
     * checks and returns if this call is a duplicate
     */
    @SubscribeEvent
    public void interceptPvP (LivingAttackEvent event)
    {
        // This alleviates duplicate entries.
        if (PvPCommonUtils.isCurrentAttackDuplicate (event))
            return;

        EntityPlayerMP attacker = PvPServerUtils.getMaster (event.source.getEntity ());
        EntityPlayerMP victim = PvPServerUtils.getMaster (event.entity);

        if (attacker == null || victim == null || attacker == victim)
            return;

        EnumPvPMode attackerMode = PvPServerUtils.getPvPMode (attacker);
        EnumPvPMode victimMode = PvPServerUtils.getPvPMode (victim);

        if (MinecraftForge.EVENT_BUS
            .post (new OnPvPEvent (attacker, attackerMode, victim, victimMode, event.ammount, event.source)))
        {
            event.setCanceled (true);
            return;
        }

        if (MinecraftForge.EVENT_BUS.post (new OnPvPEvent (attacker, attackerMode, victim, victimMode, event.ammount, event.source)))
        {
            event.setCanceled (true);
            return;
        }

        if (attackerMode != EnumPvPMode.ON)
        {
            if (attacker == event.source.getEntity ())
            {
                if (PvPServerUtils.isCreativeMode (attacker))
                {
                    ServerChatUtils.red (attacker, "You are in creative mode");
                }
                else if (PvPServerUtils.canFly (attacker))
                {
                    ServerChatUtils.red (attacker, "You are in fly mode");
                }
                else
                {
                    ServerChatUtils.red (attacker, "You have PvP disabled");
                }
            }
            event.setCanceled (true);
            return;
        }

        if (victimMode != EnumPvPMode.ON)
        {
            if (attacker == event.source.getEntity ())
            {
                ServerChatUtils.red (attacker, "This player/unit has PvP disabled");
            }
            event.setCanceled (true);
            return;
        }

        if (attacker == event.source.getEntity () && victim == event.entity)
        {
            // Both involved entities are players which can attack each other

            long time = PvPServerUtils.getTime ();

            PvPData attackerData = PvPServerUtils.getPvPData (attacker);
            PvPData victimData = PvPServerUtils.getPvPData (victim);

            if (attackerData.getPvPTimer () == 0)
            {
                ServerChatUtils.yellow (attacker, "You're now in PvP combat");
            }

            if (victimData.getPvPTimer () == 0)
            {
                ServerChatUtils.yellow (victim, "You're now in PvP combat");
            }

            attackerData.setPvPTimer (time + config.getPvPTimer ());
            victimData.setPvPTimer (time + config.getPvPTimer ());

            if (attackerData.getPvPWarmup () != 0)
            {
                attackerData.setPvPWarmup (0);
                ServerChatUtils.yellow (attacker, "Your warmup timer was canceled because you started an attack");
            }

            if (victimData.getPvPWarmup () != 0)
            {
                victimData.setPvPWarmup (0);
                ServerChatUtils.yellow (victim, "Your warmup timer was canceled because you were attacked");
            }
        }

    }

    /*
     * We need to log here because the damage in the LivingAttackEvent can change
     * from the attack to the actual application of the damage.
     */
    @SubscribeEvent
    public void onLivingHurt (LivingHurtEvent event)
    {
        EntityPlayerMP attacker = PvPServerUtils.getMaster (event.source.getEntity ());
        EntityPlayerMP victim = PvPServerUtils.getMaster (event.entity);

        if (attacker == null || victim == null)
            return;

        if (config.getActiveCombatLoggingHandlers ().size () > 0
            && !MinecraftForge.EVENT_BUS.post (new OnPvPLogEvent (attacker, victim, event.ammount, event.source)))
        {
            server.getCombatLogManager ().log (attacker, victim, event.ammount,
                event.source);
        }
    }

    /**
     * Handles PvP warmup timers.
     */
    @SubscribeEvent
    public void onPlayerTick (PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            EntityPlayer player = event.player;
            long time = PvPServerUtils.getTime ();

            PvPData data = PvPServerUtils.getPvPData (player);

            long pvpTimer = data.getPvPTimer ();

            if (!PvPServerUtils.isPvPModeOverriddenForPlayer (player) && pvpTimer == 0)
            {
                if (!PvPServerUtils.isCreativeMode (player))
                {
                    if (!PvPServerUtils.canFly (player))
                    {
                        if (config.isDefaultPvPModeForced () && !config.isPvPTogglingEnabled ())
                        {
                            if (data.isDefaultModeForced ()
                                && data.isPvPEnabled () != config.getDefaultPvPMode ().toBoolean ())
                            {
                                data.setPvPWarmup (time);
                            }
                            if (!data.isDefaultModeForced ()
                                && data.isPvPEnabled () == config.getDefaultPvPMode ().toBoolean ()
                                && data.getPvPWarmup () == 0)
                            {
                                data.setDefaultModeForced (true);
                            }
                        }

                        long toggleTime = data.getPvPWarmup ();

                        if (toggleTime != 0 && toggleTime <= time)
                        {
                            data.setPvPWarmup (0);

                            if (!data.isPvPEnabled ())
                            {
                                data.setPvPEnabled (true);
                                if (config.isPvPEnabledAnnouncedGlobally ())
                                {
                                    ServerChatUtils.postGlobalChatMessages (EnumChatFormatting.RED,
                                        "PvP is now enabled for "
                                            + player.getDisplayName ());
                                }
                                else
                                {
                                    ServerChatUtils.red (player, "PvP is now enabled for you");
                                }
                            }
                            else
                            {
                                data.setPvPEnabled (false);
                                if (config.isPvPDisabledAnnouncedGlobally ())
                                {
                                    ServerChatUtils.postGlobalChatMessages (EnumChatFormatting.GREEN,
                                        "PvP is now disabled for "
                                            + player.getDisplayName ());
                                }
                                else
                                {
                                    ServerChatUtils.green (player, "PvP is now disabled for you");
                                }
                            }

                            data.setPvPCooldown (time + config.getCooldown ());
                        }
                    }
                    else if (data.getPvPWarmup () != 0)
                    {
                        ServerChatUtils.yellow (player,
                            "Your warmup timer was canceled because you're able to fly now");
                        data.setPvPWarmup (0);
                    }
                }
                else if (data.getPvPWarmup () != 0)
                {
                    ServerChatUtils.yellow (player,
                        "Your warmup timer was canceled because you're in creative mode now");
                    data.setPvPWarmup (0);
                }
            }
            else if (pvpTimer != 0)
            {
                // The player is or was in PvP
                if (PvPServerUtils.isCreativeMode (player) || PvPServerUtils.canFly (player) || pvpTimer < time)
                {
                    // The player was in PvP or can no longer do PvP even if the timer is running
                    // yet
                    ServerChatUtils.green (player, "You're no longer in PvP combat");
                    data.setPvPTimer (0);
                }
                else
                {
                    // The player is in PvP

                    // With this event the compatibility modules can add custom behavior
                    MinecraftForge.EVENT_BUS.post (new PlayerPvPTickEvent (player));
                }

            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath (LivingDeathEvent event)
    {
        if (event.entityLiving instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            if (! (PvPServerUtils.isCreativeMode (player) || PvPServerUtils.canFly (player)))
            {
                World world = player.worldObj;
                if (world.getGameRules ().getGameRuleBooleanValue ("keepInventory"))
                {
                    if (!MinecraftForge.EVENT_BUS.post (new OnPartialInventoryLossEvent (player, event.source)))
                    {
                        boolean wasPvP = wasDeathCausedByPvP (event.source);

                        if (wasPvP && config.isPvPPartialInventoryLossEnabled ()
                            || !wasPvP && config.isPvEPartialInventoryLossEnabled ())
                        {
                            // Collect all stacks that should be dropped
                            Map<EnumInventory, TreeMap<Integer, ItemStack>> stacksToDrop = new HashMap<> ();

                            // Either use PvP or PvE inventory loss counts
                            int armorLoss = wasPvP ? config.getPvPArmourItemLoss ()
                                : config.getPvEArmourItemLoss ();
                            int hotbarLoss = wasPvP ? config.getPvPHotbarItemLoss ()
                                : config.getPvEHotbarItemLoss ();
                            int mainLoss = wasPvP ? config.getPvPMainItemLoss () : config.getPvEMainItemLoss ();

                            // Try to get the specified amount of stacks to be dropped from the inventories
                            int missingArmourStacks = dropItemsFromInventory (player, player.inventory.armorInventory,
                                0, 3,
                                armorLoss, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);
                            int missingHotbarStacks = dropItemsFromInventory (player, player.inventory.mainInventory, 0,
                                8,
                                hotbarLoss, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);
                            int missingMainStacks = dropItemsFromInventory (player, player.inventory.mainInventory, 9,
                                35,
                                mainLoss, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);

                            /*
                             * Try to get the specified amount of stacks to be dropped from other
                             * inventories if the specified inventory contains too less items.
                             */
                            if (config.isArmourInventorySearchExtended ())
                            {
                                tryOtherInventories (player, missingArmourStacks, player.inventory.mainInventory, 9, 35,
                                    player.inventory.mainInventory, 0, 8,
                                    PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER
                                        .and (PvPServerUtils.ARMOUR_FILTER),
                                    stacksToDrop);
                            }
                            if (config.isHotbarInventorySearchExtended ())
                            {
                                tryOtherInventories (player, missingHotbarStacks, player.inventory.mainInventory, 9, 35,
                                    null,
                                    -1, -1, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);
                            }
                            if (config.isMainInventorySearchExtended ())
                            {
                                tryOtherInventories (player, missingMainStacks, player.inventory.mainInventory, 0, 8,
                                    null,
                                    -1, -1, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);
                            }

                            // The stacks that should be dropped without the inventory index
                            Map<EnumInventory, List<ItemStack>> stacksToDropComputed = new HashMap<> ();

                            stacksToDrop.forEach ( (key, value) ->
                            {
                                stacksToDropComputed.put (key, new ArrayList<> (value.values ()));
                            });

                            MinecraftForge.EVENT_BUS
                                .post (new PartialItemDropEvent.Pre (player, stacksToDropComputed, wasPvP));

                            // Monitor which stacks will be deleted, dropped or remain unprocessed
                            Map<EnumInventory, List<ItemStack>> droppedStacks = new HashMap<> ();
                            Map<EnumInventory, List<ItemStack>> deletedStacks = new HashMap<> ();
                            Map<EnumInventory, List<ItemStack>> unprocessedStacks = new HashMap<> ();

                            // Execute actions like deleting or dropping stacks after all iterations
                            Collection<Runnable> actions = new ArrayList<> ();

                            stacksToDrop.forEach ( (inventory, stackMap) ->
                            {
                                for (Map.Entry<Integer, ItemStack> entry : stackMap.entrySet ())
                                {
                                    int index = entry.getKey ();
                                    ItemStack stack = entry.getValue ();

                                    PartialItemDropEvent.Drop dropEvent = new PartialItemDropEvent.Drop (player, stack,
                                        inventory);

                                    PartialItemDropEvent.Drop.Action action = PvPServerUtils.postEventAndGetResult (
                                        dropEvent,
                                        dropEvent::getAction);

                                    // Categorize and process the stacks based on the action
                                    if (action == Action.DELETE || action == Action.DELETE_AND_DROP)
                                    {
                                        addStack (deletedStacks, inventory, stack);
                                        actions.add ( () ->
                                        {
                                            // Delete the item from the player's inventory
                                            getPlayerInventoryFromEnum (player, inventory)[index] = null;
                                        });
                                    }
                                    if (action == Action.DROP || action == Action.DELETE_AND_DROP)
                                    {
                                        addStack (droppedStacks, inventory, stack);
                                        actions.add ( () ->
                                        {
                                            // Drops the item in the world
                                            player.func_146097_a (stack, true, false);
                                        });
                                    }
                                    if (action == Action.NOTHING)
                                    {
                                        addStack (unprocessedStacks, inventory, stack);
                                    }
                                }
                            });

                            actions.forEach (Runnable::run); // Execute these actions now

                            MinecraftForge.EVENT_BUS
                                .post (new PartialItemDropEvent.Post (player, droppedStacks, deletedStacks,
                                    unprocessedStacks));
                        }
                    }
                }
            }
            PvPServerUtils.getPvPData (player).setPvPTimer (0);
        }
    }

    private boolean wasDeathCausedByPvP (DamageSource source)
    {
        Entity killer = source.getEntity ();
        if (killer != null)
        {
            if (config.isIndirectPvPAllowed ())
                return PvPServerUtils.getMaster (killer) != null;
            else return killer instanceof EntityPlayer;
        }
        return false;
    }

    private int tryOtherInventories (EntityPlayer player, int inventoryLoss, ItemStack[] firstInventory,
        int firstStartIndex, int firstEndIndex, ItemStack[] secondInventory, int secondStartIndex, int secondEndIndex,
        Predicate<ItemStack> filter, Map<EnumInventory, TreeMap<Integer, ItemStack>> droppedItems)
    {
        if (inventoryLoss > 0)
        {
            int missingStacks = dropItemsFromInventory (player, firstInventory, firstStartIndex, firstEndIndex,
                inventoryLoss, filter, droppedItems);

            if (missingStacks > 0 && secondInventory != null)
                return dropItemsFromInventory (player, secondInventory, secondStartIndex,
                    secondEndIndex,
                    missingStacks, filter, droppedItems);
            return missingStacks;
        }
        return inventoryLoss;
    }

    private int dropItemsFromInventory (EntityPlayer player, ItemStack[] inventory, int startIndex, int endIndex,
        int inventoryLoss, Predicate<ItemStack> filter, Map<EnumInventory, TreeMap<Integer, ItemStack>> droppedItems)
    {
        List<Integer> filledInventorySlots = new ArrayList<> (PvPServerUtils
            .getFilledInventorySlots (inventory, startIndex, endIndex, filter));

        int size = filledInventorySlots.size (); // The size of the list itself decreases every iteration
        for (int i = 0; i < Math.min (inventoryLoss, size); i++)
        {
            int randomSlotIndex = MathHelper.getRandomIntegerInRange (random, 0, filledInventorySlots.size () - 1);
            int randomSlot = filledInventorySlots.remove (randomSlotIndex);

            EnumInventory enumInventory = getEnumFromPlayerInventory (player, inventory, randomSlot);

            if (!droppedItems.containsKey (enumInventory))
            {
                droppedItems.put (enumInventory, new TreeMap<> ());
            }
            droppedItems.get (enumInventory).put (randomSlot, inventory[randomSlot]);
        }
        return Math.max (0, inventoryLoss - size); // Returns the count of stacks which still have to be dropped
    }

    private ItemStack[] getPlayerInventoryFromEnum (EntityPlayer player, EnumInventory inventory)
    {
        switch (inventory)
        {
            case ARMOUR:
                return player.inventory.armorInventory;
            default:
                return player.inventory.mainInventory;
        }
    }

    private EnumInventory getEnumFromPlayerInventory (EntityPlayer player, ItemStack[] inventory, int index)
    {
        if (inventory == player.inventory.armorInventory)
            return EnumInventory.ARMOUR;
        else
        {
            if (index == player.inventory.currentItem)
                return EnumInventory.HELD;
            else if (index < 9)
                return EnumInventory.HOTBAR;
            else return EnumInventory.MAIN;
        }
    }

    private void addStack (Map<EnumInventory, List<ItemStack>> map, EnumInventory inventory, ItemStack stack)
    {
        if (!map.containsKey (inventory))
        {
            map.put (inventory, new ArrayList<> ());
        }
        map.get (inventory).add (stack);
    }

    @SubscribeEvent
    public void onCommandExecution (CommandEvent event)
    {
        // Cancel blacklisted commands for players in PvP
        if (event.sender instanceof EntityPlayerMP)
        {
            if (PvPServerUtils.isInPvP ((EntityPlayer) event.sender))
            {
                for (String command : config.getBlockedCommands ())
                {
                    if (PvPServerUtils.matches (event.command, command))
                    {
                        // The command is blacklisted and will be canceled
                        event.setCanceled (true);
                        ServerChatUtils.red (event.sender, "You cannot use this command while in PvP combat");
                        return;
                    }
                }
            }
        }
    }

    private Map<EntityPlayer, Collection<ItemStack>> soulboundItems = new HashMap<> ();

    @SubscribeEvent
    public void onPlayerDrops (PlayerDropsEvent event)
    {
        if (config.areSoulboundItemsEnabled ())
        {
            ListIterator<EntityItem> drops = event.drops.listIterator ();
            while (drops.hasNext ())
            {
                ItemStack drop = drops.next ().getEntityItem ();
                if (PvPServerUtils.isSoulbound (drop))
                {
                    drops.remove ();
                    if (!soulboundItems.containsKey (event.entityPlayer))
                    {
                        soulboundItems.put (event.entityPlayer, new ArrayList<> ());
                    }
                    soulboundItems.get (event.entityPlayer).add (drop);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn (PlayerRespawnEvent event)
    {
        if (config.areSoulboundItemsEnabled () && soulboundItems.containsKey (event.player))
        {
            soulboundItems.get (event.player).forEach (event.player.inventory::addItemStackToInventory);
            soulboundItems.remove (event.player);
        }
    }

    @SubscribeEvent
    public void onPartialItemLoss (PartialItemLossEvent event)
    {
        event.setCanceled (config.areSoulboundItemsEnabled () && PvPServerUtils.isSoulbound (event.getStack ()));
    }

    // @SubscribeEvent
    // public void onItemTooltip (ItemTooltipEvent event)
    // {
    // if (PvPUtils.isSoulbound (event.itemStack) && PvPMode.soulboundItemsEnabled)
    // {
    // event.toolTip.add (PvPMode.soulboundTooltip);
    // }
    // }TO

    @SubscribeEvent
    public void onPlayerLoggedIn (PlayerLoggedInEvent event)
    {
        if (config.isVersionCheckerEnabled () && config.isNewVersionAnnouncedInChat ()
            && PvPServerUtils.isOpped (event.player)
            && server.getVersionComparison () == EnumVersionComparison.NEWER)
        {
            RemoteVersion version = server.getRemoteVersion ();

            ChatComponentText messagePart0 = new ChatComponentText ("[Server]: ");
            ChatComponentText messagePart1 = new ChatComponentText ("A ");
            ChatComponentText messagePart2 = new ChatComponentText ("new version");
            ChatComponentText messagePart3 = new ChatComponentText (" (");
            ChatComponentText messagePart4 = new ChatComponentText (
                server.getRemoteVersion ().getRemoteVersion ().toString ());
            ChatComponentText messagePart5 = new ChatComponentText (
                ") of the PvP Mode Mod is available.");

            messagePart0.getChatStyle ().setColor (EnumChatFormatting.GOLD);
            messagePart1.getChatStyle ().setColor (EnumChatFormatting.YELLOW);

            if (version.getChangelogURL () != null)
            {
                messagePart2.getChatStyle ().setColor (EnumChatFormatting.BLUE);
                messagePart2.getChatStyle ()
                    .setChatClickEvent (
                        new ClickEvent (ClickEvent.Action.OPEN_URL, version.getChangelogURL ().toString ()));
                messagePart2.getChatStyle ()
                    .setChatHoverEvent (
                        new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText ("Click to open the changelog")));
            }
            else
            {
                messagePart2.getChatStyle ().setColor (EnumChatFormatting.YELLOW);
            }

            messagePart3.getChatStyle ().setColor (EnumChatFormatting.YELLOW);

            if (version.getDownloadURL () != null)
            {
                messagePart4.getChatStyle ().setColor (EnumChatFormatting.GREEN);
                messagePart4.getChatStyle ()
                    .setChatClickEvent (
                        new ClickEvent (ClickEvent.Action.OPEN_URL, version.getDownloadURL ().toString ()));
                messagePart4.getChatStyle ()
                    .setChatHoverEvent (
                        new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText ("Click to open the download page of the new version")));
            }
            else
            {
                messagePart4.getChatStyle ().setColor (EnumChatFormatting.YELLOW);
            }

            messagePart5.getChatStyle ().setColor (EnumChatFormatting.YELLOW);

            event.player.addChatComponentMessage (
                messagePart0.appendSibling (messagePart1.appendSibling (messagePart2).appendSibling (messagePart3)
                    .appendSibling (messagePart4).appendSibling (messagePart5)));
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut (PlayerLoggedOutEvent event)
    {
        server.getClientsideSupportHandler ().removeSupportedClient ((EntityPlayerMP) event.player);
    }

}
