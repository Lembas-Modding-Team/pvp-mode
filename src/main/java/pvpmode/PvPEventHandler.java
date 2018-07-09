package pvpmode;

import java.util.*;
import java.util.function.Predicate;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.*;
import pvpmode.compatibility.events.*;

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

        if (attacker == null || victim == null)
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
                // Both involved entities are players which can attack each
                // other

                long time = PvPUtils.getTime ();

                PvPData attackerData = PvPUtils.getPvPData (attacker);
                PvPData victimData = PvPUtils.getPvPData (victim);

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
            long toggleTime = data.getPvPWarmup ();

            if (!PvPUtils.isPvPModeOverriddenForPlayer (player) && pvpTimer == 0)
            {
                if (!PvPUtils.isCreativeMode (player))
                {
                    if (!PvPUtils.canFly (player))
                    {
                        if (toggleTime != 0 && toggleTime < time)
                        {
                            data.setPvPWarmup (0);

                            if (!data.isPvPEnabled ())
                            {
                                data.setPvPEnabled (true);
                                ChatUtils.postGlobalChatMessages (EnumChatFormatting.RED, "PvP is now enabled for "
                                    + player.getDisplayName ());
                            }
                            else
                            {
                                data.setPvPEnabled (false);
                                ChatUtils.green (player, "PvP is now disabled for you");
                            }

                            data.setPvPCooldown (time + PvPMode.cooldown);
                        }
                    }
                    else if (toggleTime != 0)
                    {
                        ChatUtils.yellow (player, "Your warmup timer was canceled because you're able to fly now");
                        data.setPvPWarmup (0);
                    }
                }
                else if (toggleTime != 0)
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
                    // The player was in PvP or can no longer do PvP
                    // even if
                    // the
                    // timer is running yet
                    ChatUtils.green (player, "You're no longer in PvP");
                    data.setPvPTimer (0);
                }
                else
                {
                    // The player is in PvP

                    // With this event the compatibility modules can add
                    // custom
                    // behavior
                    MinecraftForge.EVENT_BUS.post (new PlayerPvPTickEvent (player));
                }

            }
        }
    }

    @SubscribeEvent
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
                            // Either use PvP or PvE inventory loss counts
                            int armorLoss = wasPvP ? PvPMode.inventoryLossArmour : PvPMode.inventoryLossArmourPvE;
                            int hotbarLoss = wasPvP ? PvPMode.inventoryLossHotbar : PvPMode.inventoryLossHotbarPvE;
                            int mainLoss = wasPvP ? PvPMode.inventoryLossMain : PvPMode.inventoryLossMainPvE;

                            // Try to drop the specified amount of stacks from the inventories
                            int missingArmourStacks = dropItemsFromInventory (player, player.inventory.armorInventory,
                                0, 3,
                                armorLoss, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);
                            int missingHotbarStacks = dropItemsFromInventory (player, player.inventory.mainInventory, 0,
                                8,
                                hotbarLoss, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);
                            int missingMainStacks = dropItemsFromInventory (player, player.inventory.mainInventory, 9,
                                35,
                                mainLoss, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);

                            /*
                             * Try to drop the specified amount of stacks from other inventories if the
                             * specified inventory contains too less items.
                             */
                            if (PvPMode.extendArmourInventorySearch)
                                tryOtherInventories (player, missingArmourStacks, player.inventory.mainInventory, 9, 35,
                                    player.inventory.mainInventory, 0, 8,
                                    PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER.and (PvPUtils.ARMOUR_FILTER));
                            if (PvPMode.extendHotbarInventorySearch)
                                tryOtherInventories (player, missingHotbarStacks, player.inventory.mainInventory, 9, 35,
                                    null,
                                    -1, -1, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);
                            if (PvPMode.extendMainInventorySearch)
                                tryOtherInventories (player, missingMainStacks, player.inventory.mainInventory, 0, 8,
                                    null,
                                    -1, -1, PvPUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);
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
            {
                return PvPUtils.getMaster (killer) != null;
            }
            else
            {
                return killer instanceof EntityPlayer;
            }
        }
        return false;
    }

    private int tryOtherInventories (EntityPlayer player, int inventoryLoss, ItemStack[] firstInventory,
        int firstStartIndex, int firstEndIndex, ItemStack[] secondInventory, int secondStartIndex, int secondEndIndex,
        Predicate<ItemStack> filter)
    {
        if (inventoryLoss > 0)
        {
            int missingStacks = dropItemsFromInventory (player, firstInventory, firstStartIndex, firstEndIndex,
                inventoryLoss, filter);

            if (missingStacks > 0 && secondInventory != null)
                return dropItemsFromInventory (player, secondInventory, secondStartIndex,
                    secondEndIndex,
                    missingStacks, filter);
            return missingStacks;
        }
        return inventoryLoss;
    }

    private int dropItemsFromInventory (EntityPlayer player, ItemStack[] inventory, int startIndex, int endIndex,
        int inventoryLoss, Predicate<ItemStack> filter)
    {
        List<Integer> filledInventorySlots = new ArrayList<> (PvPUtils
            .getFilledInventorySlots (inventory, startIndex, endIndex, filter));
        int size = filledInventorySlots.size ();// The size of the list itself
                                                // decreases every iteration
        for (int i = 0; i < Math.min (inventoryLoss, size); i++)
        {
            int randomSlotIndex = MathHelper.getRandomIntegerInRange (random, 0, filledInventorySlots.size () - 1);
            int randomSlot = filledInventorySlots.remove (randomSlotIndex);
            player.func_146097_a (inventory[randomSlot], true, false); // Drops
                                                                       // the
                                                                       // item
                                                                       // in the
                                                                       // world
            inventory[randomSlot] = null; // Make sure to delete the item from
                                          // the player's inventory
        }
        return Math.max (0, inventoryLoss - size); // Returns the count of stacks which still have to be dropped
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
                        ChatUtils.red (event.sender, "You cannot use this command while in PvP");
                        return;
                    }
                }
            }
        }
    }

    public static void init ()
    {
        INSTANCE = new PvPEventHandler ();
        MinecraftForge.EVENT_BUS.register (INSTANCE);
        FMLCommonHandler.instance ().bus ().register (INSTANCE);
    }

}
