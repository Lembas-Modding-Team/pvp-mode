package pvpmode;

import java.util.*;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.*;
import pvpmode.compatibility.events.PlayerPvPTickEvent;

public class PvPEventHandler
{
    public static PvPEventHandler INSTANCE;

    private Random random = new Random ();

    /**
     * Cancels combat events associated with PvP-disabled players. Note that
     * this function will be invoked twice per attack - this is because of a
     * Forge bug.
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
                    PvPUtils.red (attacker, "You are in creative mode!");
                }
                else if (PvPUtils.canFly (attacker))
                {
                    PvPUtils.red (attacker, "You are in fly mode!");
                }
                else
                {
                    PvPUtils.red (attacker, "You have PvP disabled!");
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
                PvPUtils.red (attacker, "This player/unit has PvP disabled!");
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

                attackerData.setPvPWarmup (0);
                victimData.setPvPWarmup (0);
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

        if (PvPMode.activatedPvPLoggingHandlers.size () > 0)
            PvPMode.combatLogManager.log (attacker, victim, event.ammount, event.source);
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

            if (!PvPUtils.isCreativeMode (player))
            {
                if (!PvPUtils.canFly (player))
                {
                    if (!PvPUtils.isPvPModeOverriddenForPlayer (data) && pvpTimer == 0)
                    {
                        if (toggleTime != 0 && toggleTime < time)
                        {
                            data.setPvPWarmup (0);

                            if (!data.isPvPEnabled ())
                            {
                                data.setPvPEnabled (true);
                                PvPMode.cfg.sendChatMsg (new ChatComponentText (
                                    EnumChatFormatting.RED + "WARNING: PvP is now enabled for "
                                        + player.getDisplayName ()
                                        + "!"));
                            }
                            else
                            {
                                data.setPvPEnabled (false);
                                PvPUtils.green (player, "PvP is now disabled for you.");
                            }

                            data.setPvPCooldown (time + PvPMode.cooldown);
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
                            PvPUtils.green (player, "You're no longer in PvP");
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
                else if (toggleTime != 0)
                {
                    PvPUtils.yellow (player, "Your warmup timer was resetted because you're able to fly now");
                    data.setPvPWarmup (0);
                }
            }
            else if (toggleTime != 0)
            {
                PvPUtils.yellow (player, "Your warmup timer was resetted because you're in creative mode now");
                data.setPvPWarmup (0);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath (LivingDeathEvent event)
    {
        if (PvPMode.partialInventoryLossEnabled)
        {
            if (event.entityLiving instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) event.entityLiving;
                if (! (PvPUtils.isCreativeMode (player) || PvPUtils.canFly (player)))
                {
                    World world = player.worldObj;
                    if (world.getGameRules ().getGameRuleBooleanValue ("keepInventory"))
                    {
                        dropItemsFromInventory (player, player.inventory.armorInventory, 0, 3,
                            PvPMode.inventoryLossArmour);
                        dropItemsFromInventory (player, player.inventory.mainInventory, 0, 8,
                            PvPMode.inventoryLossHotbar);
                    }
                }
            }
        }
    }

    private void dropItemsFromInventory (EntityPlayer player, ItemStack[] inventory, int startIndex, int endIndex,
        int inventoryLoss)
    {
        List<Integer> filledArmorSlots = new ArrayList<> (PvPUtils
            .getFilledInventorySlots (inventory, startIndex, endIndex));
        for (int i = 0; i < Math.min (inventoryLoss, filledArmorSlots.size ()); i++)
        {
            int randomSlotIndex = MathHelper.getRandomIntegerInRange (random, 0, filledArmorSlots.size () - 1);
            int randomSlot = filledArmorSlots.remove (randomSlotIndex);
            player.func_146097_a (inventory[randomSlot], true, false); // Drops
                                                                       // the
                                                                       // item
                                                                       // in the
                                                                       // world
            inventory[randomSlot] = null; // Make sure to delete the item from
                                          // the player's inventory
        }
    }

    @SubscribeEvent
    public void onCommandExecution (CommandEvent event)
    {
        // Cancel blacklisted commands for players in PvP
        if (event.sender instanceof EntityPlayerMP)
        {
            if (PvPUtils.isInPvP (PvPUtils.getPvPData ((EntityPlayer) event.sender)))
            {
                for (String command : PvPMode.commandBlacklist)
                {
                    if (PvPUtils.matches (event.command, command))
                    {
                        // The command is blacklisted and will be canceled
                        event.setCanceled (true);
                        PvPUtils.red (event.sender, "You cannot use this command while in PvP.");
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
