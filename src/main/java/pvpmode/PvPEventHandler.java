package pvpmode;

import java.util.*;
import java.util.function.Predicate;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import pvpmode.compatibility.events.*;
import pvpmode.compatibility.events.PartialItemDropEvent.Drop.Action;
import pvpmode.compatibility.events.PartialItemDropEvent.EnumInventory;

public class PvPEventHandler
{
    public static PvPEventHandler INSTANCE;

    private Random random = new Random ();

    /**
     * Cancels combat events associated with PvP-disabled players. Note that this
     * function will be invoked twice per attack - this is because of a Forge bug.
     */
    @SubscribeEvent
    public void interceptPvP (LivingAttackEvent event)
    {
        EntityPlayerMP attacker = PvPUtils.getMaster (event.source.getEntity ());
        EntityPlayerMP victim = PvPUtils.getMaster (event.entity);

        if (attacker == null || victim == null || attacker == victim)
            return;

        EnumPvPMode attackerMode = PvPUtils.getPvPMode (attacker);
        EnumPvPMode victimMode = PvPUtils.getPvPMode (victim);

        boolean cancel = false;

        if (attackerMode != EnumPvPMode.ON)
        {
            if (attacker == event.source.getEntity ())
            {
                if (PvPUtils.isCreativeMode (attacker))
                {
                    ChatUtils.red (attacker, "You are in creative mode");
                }
                else if (PvPUtils.canFly (attacker))
                {
                    ChatUtils.red (attacker, "You are in fly mode");
                }
                else
                {
                    ChatUtils.red (attacker, "You have PvP disabled");
                }
            }
            cancel = true;
        }

        if (cancel)
        {// For performance reasons
            event.setCanceled (true);
            return;
        }

        if (victimMode != EnumPvPMode.ON)
        {
            if (attacker == event.source.getEntity ())
            {
                ChatUtils.red (attacker, "This player/unit has PvP disabled");
            }
            cancel = true;
        }

        if (cancel)
        {
            event.setCanceled (true);
        }
        else
        {
            if (attacker == event.source.getEntity () && victim == event.entity)
            {
                // Both involved entities are players which can attack each other

                long time = PvPUtils.getTime ();

                PvPData attackerData = PvPUtils.getPvPData (attacker);
                PvPData victimData = PvPUtils.getPvPData (victim);

                if (attackerData.getPvPTimer () == 0)
                {
                    ChatUtils.yellow (attacker, "You're now in PvP combat");
                }

                if (victimData.getPvPTimer () == 0)
                {
                    ChatUtils.yellow (victim, "You're now in PvP combat");
                }

                attackerData.setPvPTimer (time + PvPMode.pvpTimer);
                victimData.setPvPTimer (time + PvPMode.pvpTimer);

                if (attackerData.getPvPWarmup () != 0)
                {
                    attackerData.setPvPWarmup (0);
                    ChatUtils.yellow (attacker, "Your warmup timer was canceled because you started an attack");
                }

                if (victimData.getPvPWarmup () != 0)
                {
                    victimData.setPvPWarmup (0);
                    ChatUtils.yellow (victim, "Your warmup timer was canceled because you were attacked");
                }
            }
        }

    }

    /*
     * We need to log here because the LivingAttackEvent will be fired twice per
     * attack.
     */
    @SubscribeEvent
    public void onLivingHurt (LivingHurtEvent event)
    {
        EntityPlayerMP attacker = PvPUtils.getMaster (event.source.getEntity ());
        EntityPlayerMP victim = PvPUtils.getMaster (event.entity);

        if (attacker == null || victim == null)
            return;

        if (PvPMode.activatedPvPLoggingHandlers.size () > 0
            && !MinecraftForge.EVENT_BUS.post (new OnPvPLogEvent (attacker, victim, event.ammount, event.source)))
        {
            PvPMode.combatLogManager.log (attacker, victim, event.ammount, event.source);
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
            long time = PvPUtils.getTime ();

            PvPData data = PvPUtils.getPvPData (player);

            long pvpTimer = data.getPvPTimer ();

            if (!PvPUtils.isPvPModeOverriddenForPlayer (player) && pvpTimer == 0)
            {
                if (!PvPUtils.isCreativeMode (player))
                {
                    if (!PvPUtils.canFly (player))
                    {
                        if (PvPMode.forceDefaultPvPMode && !PvPMode.pvpTogglingEnabled)
                        {
                            if (data.isDefaultModeForced () && data.isPvPEnabled () != PvPMode.defaultPvPMode)
                            {
                                data.setPvPWarmup (time);
                            }
                            if (!data.isDefaultModeForced () && data.isPvPEnabled () == PvPMode.defaultPvPMode
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
                                if (PvPMode.announcePvPEnabledGlobally)
                                {
                                    ChatUtils.postGlobalChatMessages (EnumChatFormatting.RED, "PvP is now enabled for "
                                        + player.getDisplayName ());
                                }
                                else
                                {
                                    ChatUtils.red (player, "PvP is now enabled for you");
                                }
                            }
                            else
                            {
                                data.setPvPEnabled (false);
                                if (PvPMode.announcePvPDisabledGlobally)
                                {
                                    ChatUtils.postGlobalChatMessages (EnumChatFormatting.GREEN,
                                        "PvP is now disabled for "
                                            + player.getDisplayName ());
                                }
                                else
                                {
                                    ChatUtils.green (player, "PvP is now disabled for you");
                                }
                            }

                            data.setPvPCooldown (time + PvPMode.cooldown);
                        }
                    }
                    else if (data.getPvPWarmup () != 0)
                    {
                        ChatUtils.yellow (player, "Your warmup timer was canceled because you're able to fly now");
                        data.setPvPWarmup (0);
                    }
                }
                else if (data.getPvPWarmup () != 0)
                {
                    ChatUtils.yellow (player, "Your warmup timer was canceled because you're in creative mode now");
                    data.setPvPWarmup (0);
                }
            }
            else if (pvpTimer != 0)
            {
                // The player is or was in PvP
                if (PvPUtils.isCreativeMode (player) || PvPUtils.canFly (player) || pvpTimer < time)
                {
                    // The player was in PvP or can no longer do PvP even if the timer is running
                    // yet
                    ChatUtils.green (player, "You're no longer in PvP combat");
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
            if (! (PvPUtils.isCreativeMode (player) || PvPUtils.canFly (player)))
            {
                World world = player.worldObj;
                if (world.getGameRules ().getGameRuleBooleanValue ("keepInventory"))
                {
                    if (!MinecraftForge.EVENT_BUS.post (new OnPartialInventoryLossEvent (player, event.source)))
                    {
                        boolean wasPvP = wasDeathCausedByPvP (event.source);

                        if (wasPvP && PvPMode.partialInventoryLossEnabled
                            || !wasPvP && PvPMode.enablePartialInventoryLossPvE)
                        {
                            // Collect all stacks that should be dropped
                            Map<EnumInventory, TreeMap<Integer, ItemStack>> stacksToDrop = new HashMap<> ();

                            // Either use PvP or PvE inventory loss counts
                            int armorLoss = wasPvP ? PvPMode.inventoryLossArmour : PvPMode.inventoryLossArmourPvE;
                            int hotbarLoss = wasPvP ? PvPMode.inventoryLossHotbar : PvPMode.inventoryLossHotbarPvE;
                            int mainLoss = wasPvP ? PvPMode.inventoryLossMain : PvPMode.inventoryLossMainPvE;

                            // Try to get the specified amount of stacks to be dropped from the inventories
                            int missingArmourStacks = dropItemsFromInventory (player, player.inventory.armorInventory,
                                0, 3,
                                armorLoss, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);
                            int missingHotbarStacks = dropItemsFromInventory (player, player.inventory.mainInventory, 0,
                                8,
                                hotbarLoss, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);
                            int missingMainStacks = dropItemsFromInventory (player, player.inventory.mainInventory, 9,
                                35,
                                mainLoss, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);

                            /*
                             * Try to get the specified amount of stacks to be dropped from other
                             * inventories if the specified inventory contains too less items.
                             */
                            if (PvPMode.extendArmourInventorySearch)
                            {
                                tryOtherInventories (player, missingArmourStacks, player.inventory.mainInventory, 9, 35,
                                    player.inventory.mainInventory, 0, 8,
                                    PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER.and (PvPUtils.ARMOUR_FILTER),
                                    stacksToDrop);
                            }
                            if (PvPMode.extendHotbarInventorySearch)
                            {
                                tryOtherInventories (player, missingHotbarStacks, player.inventory.mainInventory, 9, 35,
                                    null,
                                    -1, -1, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);
                            }
                            if (PvPMode.extendMainInventorySearch)
                            {
                                tryOtherInventories (player, missingMainStacks, player.inventory.mainInventory, 0, 8,
                                    null,
                                    -1, -1, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER, stacksToDrop);
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

                                    PartialItemDropEvent.Drop.Action action = PvPUtils.postEventAndGetResult (dropEvent,
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
            PvPUtils.getPvPData (player).setPvPTimer (0);
        }
    }

    private boolean wasDeathCausedByPvP (DamageSource source)
    {
        Entity killer = source.getEntity ();
        if (killer != null)
        {
            if (PvPMode.allowIndirectPvP)
                return PvPUtils.getMaster (killer) != null;
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
        List<Integer> filledInventorySlots = new ArrayList<> (PvPUtils
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
            if (PvPUtils.isInPvP ((EntityPlayer) event.sender))
            {
                for (String command : PvPMode.commandBlacklist)
                {
                    if (PvPUtils.matches (event.command, command))
                    {
                        // The command is blacklisted and will be canceled
                        event.setCanceled (true);
                        ChatUtils.red (event.sender, "You cannot use this command while in PvP combat");
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
        if (PvPMode.soulboundItemsEnabled)
        {
            ListIterator<EntityItem> drops = event.drops.listIterator ();
            while (drops.hasNext ())
            {
                ItemStack drop = drops.next ().getEntityItem ();
                if (PvPUtils.isSoulbound (drop))
                {
                    drops.remove ();
                    if (soulboundItems.containsKey (event.entityPlayer))
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
        if (PvPMode.soulboundItemsEnabled && soulboundItems.containsKey (event.player))
        {
            soulboundItems.get (event.player).forEach (event.player.inventory::addItemStackToInventory);
            soulboundItems.remove (event.player);
        }
    }

    @SubscribeEvent
    public void onPartialItemLoss (PartialItemLossEvent event)
    {
        event.setCanceled (PvPMode.soulboundItemsEnabled && PvPUtils.isSoulbound (event.getStack ()));
    }

//    @SubscribeEvent
//    public void onItemTooltip (ItemTooltipEvent event)
//    {
//        if (PvPUtils.isSoulbound (event.itemStack) && PvPMode.soulboundItemsEnabled)
//        {
//            event.toolTip.add (PvPMode.soulboundTooltip);
//        }
//    }TODO: clientside mod

    public static void init ()
    {
        INSTANCE = new PvPEventHandler ();
        MinecraftForge.EVENT_BUS.register (INSTANCE);
        FMLCommonHandler.instance ().bus ().register (INSTANCE);
    }

}
