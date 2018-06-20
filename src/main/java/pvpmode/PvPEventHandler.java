package pvpmode;

import java.util.*;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import pvpmode.compatibility.events.EntityMasterExtractionEvent;

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
        EntityPlayerMP attacker = getMaster (event.source.getEntity ());
        EntityPlayerMP victim = getMaster (event.entity);

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

        if (cancel) {// For performance reasons
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
            event.setCanceled (true);
    }

    /*
     * We need to log here because the LivingAttackEvent will be fired twice per
     * attack.
     */
    @SubscribeEvent
    public void onLivingHurt (LivingHurtEvent event)
    {
        EntityPlayerMP attacker = getMaster (event.source.getEntity ());
        EntityPlayerMP victim = getMaster (event.entity);

        if (attacker == null || victim == null)
            return;

        if (PvPMode.activatedPvPLoggingHandlers.size () > 0)
            PvPMode.combatLogManager.log (attacker, victim, event.ammount, event.source);
    }

    /**
     * Handles PvP warmup timers.
     */
    @SubscribeEvent
    public void onLivingUpdate (LivingUpdateEvent event)
    {
        EntityPlayerMP player;
        long time = PvPUtils.getTime ();

        if (event.entityLiving instanceof EntityPlayerMP)
            player = (EntityPlayerMP) event.entityLiving;
        else return;

        PvPData data = PvPUtils.getPvPData (player);

        if (!PvPUtils.isPvPModeOverriddenForPlayer (data))
        {
            long toggleTime = data.getPvPWarmup ();

            if (toggleTime != 0 && toggleTime < time)
            {
                data.setPvPWarmup (0);

                if (!data.isPvPEnabled ())
                {
                    data.setPvPEnabled (true);
                    PvPMode.cfg.sendChatMsg (new ChatComponentText (
                        EnumChatFormatting.RED + "WARNING: PvP is now enabled for " + player.getDisplayName () + "!"));
                }
                else
                {
                    data.setPvPEnabled (false);
                    PvPUtils.green (player, "PvP is now disabled for you.");
                }

                data.setPvPCooldown (time + PvPMode.cooldown);
            }
        }
    }

    /**
     * Returns the player that this entity is associated with, if possible.
     */
    public EntityPlayerMP getMaster (Entity entity)
    {
        if (entity == null)
            return null;

        if (entity instanceof EntityPlayerMP)
            return (EntityPlayerMP) entity;

        if (entity instanceof IEntityOwnable)
            return (EntityPlayerMP) ((IEntityOwnable) entity).getOwner ();

        //Via this event the compatibility modules will be asked to extract the master
        EntityMasterExtractionEvent event = new EntityMasterExtractionEvent (entity);
        return PvPUtils.postEventAndGetResult (event, event::getMaster);
    }

    @SubscribeEvent
    public void onPlayerDeath (LivingDeathEvent event)
    {
        if (PvPMode.partialInventoryLossEnabled)
        {
            if (event.entityLiving instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) event.entityLiving;
                if (!(PvPUtils.isCreativeMode (player) || PvPUtils.canFly (player)))
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
            int randomSlotIndex = MathHelper.getRandomIntegerInRange (random, 0, filledArmorSlots.size ()-1);
            int randomSlot = filledArmorSlots.remove (randomSlotIndex);
            player.func_146097_a (inventory[randomSlot], true, false); // Drops the item in the world
            inventory[randomSlot] = null; // Make sure to delete the item from the player's inventory
        }
    }

    public static void init ()
    {
        INSTANCE = new PvPEventHandler ();
        MinecraftForge.EVENT_BUS.register (INSTANCE);
    }

}
