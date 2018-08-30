package pvpmode.internal.server;

import java.util.*;
import java.util.function.Predicate;

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
import pvpmode.PvPMode;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.compatibility.events.*;
import pvpmode.api.server.utils.*;

public class PvPServerEventHandler
{
    private final ServerProxy server;

    private final Random random = new Random ();

    public PvPServerEventHandler ()
    {
        server = PvPMode.instance.getServerProxy ();
    }

    /**
     * Cancels combat events associated with PvP-disabled players. Note that this
     * function will be invoked twice per attack - this is because of a Forge bug.
     */
    @SubscribeEvent
    public void interceptPvP (LivingAttackEvent event)
    {
        EntityPlayerMP attacker = PvPServerUtils.getMaster (event.source.getEntity ());
        EntityPlayerMP victim = PvPServerUtils.getMaster (event.entity);

        if (attacker == null || victim == null)
            return;

        EnumPvPMode attackerMode = PvPServerUtils.getPvPMode (attacker);
        EnumPvPMode victimMode = PvPServerUtils.getPvPMode (victim);

        boolean cancel = false;

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
                ServerChatUtils.red (attacker, "This player/unit has PvP disabled");
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

                attackerData.setPvPTimer (time + server.getPvpTimer ());
                victimData.setPvPTimer (time + server.getPvpTimer ());

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

    }

    /*
     * We need to log here because the LivingAttackEvent will be fired twice per
     * attack.
     */
    @SubscribeEvent
    public void onLivingHurt (LivingHurtEvent event)
    {
        EntityPlayerMP attacker = PvPServerUtils.getMaster (event.source.getEntity ());
        EntityPlayerMP victim = PvPServerUtils.getMaster (event.entity);

        if (attacker == null || victim == null)
            return;

        if (server.getActivatedPvPLoggingHandlers ().size () > 0
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
                        if (server.isForceDefaultPvPMode () && !server.isPvpTogglingEnabled ())
                        {
                            if (data.isDefaultModeForced () && data.isPvPEnabled () != server.getDefaultPvPMode ())
                            {
                                data.setPvPWarmup (time);
                            }
                            if (!data.isDefaultModeForced () && data.isPvPEnabled () == server.getDefaultPvPMode ()
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
                                if (server.isAnnouncePvPEnabledGlobally ())
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
                                if (server.isAnnouncePvPDisabledGlobally ())
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

                            data.setPvPCooldown (time + server.getCooldown ());
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

    @SubscribeEvent
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

                        if (wasPvP && server.isPartialInventoryLossEnabled ()
                            || !wasPvP && server.isEnablePartialInventoryLossPvE ())
                        {
                            // Either use PvP or PvE inventory loss counts
                            int armorLoss = wasPvP ? server.getInventoryLossArmour ()
                                : server.getInventoryLossArmourPvE ();
                            int hotbarLoss = wasPvP ? server.getInventoryLossHotbar ()
                                : server.getInventoryLossHotbarPvE ();
                            int mainLoss = wasPvP ? server.getInventoryLossMain () : server.getInventoryLossMainPvE ();

                            // Try to drop the specified amount of stacks from the inventories
                            int missingArmourStacks = dropItemsFromInventory (player, player.inventory.armorInventory,
                                0, 3,
                                armorLoss, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);
                            int missingHotbarStacks = dropItemsFromInventory (player, player.inventory.mainInventory, 0,
                                8,
                                hotbarLoss, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);
                            int missingMainStacks = dropItemsFromInventory (player, player.inventory.mainInventory, 9,
                                35,
                                mainLoss, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);

                            /*
                             * Try to drop the specified amount of stacks from other inventories if the
                             * specified inventory contains too less items.
                             */
                            if (server.isExtendArmourInventorySearch ())
                            {
                                tryOtherInventories (player, missingArmourStacks, player.inventory.mainInventory, 9, 35,
                                    player.inventory.mainInventory, 0, 8,
                                    PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER
                                        .and (PvPServerUtils.ARMOUR_FILTER));
                            }
                            if (server.isExtendHotbarInventorySearch ())
                            {
                                tryOtherInventories (player, missingHotbarStacks, player.inventory.mainInventory, 9, 35,
                                    null,
                                    -1, -1, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);
                            }
                            if (server.isExtendMainInventorySearch ())
                            {
                                tryOtherInventories (player, missingMainStacks, player.inventory.mainInventory, 0, 8,
                                    null,
                                    -1, -1, PvPServerUtils.PARTIAL_INVENTORY_LOSS_COMP_FILTER);
                            }
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
            if (server.isAllowIndirectPvP ())
                return PvPServerUtils.getMaster (killer) != null;
            else return killer instanceof EntityPlayer;
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
        List<Integer> filledInventorySlots = new ArrayList<> (PvPServerUtils
            .getFilledInventorySlots (inventory, startIndex, endIndex, filter));
        int size = filledInventorySlots.size ();// The size of the list itself
                                                // decreases every iteration
        for (int i = 0; i < Math.min (inventoryLoss, size); i++)
        {
            int randomSlotIndex = MathHelper.getRandomIntegerInRange (random, 0, filledInventorySlots.size () - 1);
            int randomSlot = filledInventorySlots.remove (randomSlotIndex);
            player.func_146097_a (inventory[randomSlot], true, false); // Drops the item in the world
            inventory[randomSlot] = null; // Make sure to delete the item from the player's inventory
        }
        return Math.max (0, inventoryLoss - size); // Returns the count of stacks which still have to be dropped
    }

    @SubscribeEvent
    public void onCommandExecution (CommandEvent event)
    {
        // Cancel blacklisted commands for players in PvP
        if (event.sender instanceof EntityPlayerMP)
        {
            if (PvPServerUtils.isInPvP ((EntityPlayer) event.sender))
            {
                for (String command : server.getCommandBlacklist ())
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

}
