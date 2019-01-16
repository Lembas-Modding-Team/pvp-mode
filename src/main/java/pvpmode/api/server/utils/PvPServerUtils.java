package pvpmode.api.server.utils;

import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.compatibility.events.EntityMasterExtractionEvent;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;
import pvpmode.api.server.compatibility.events.PlayerIdentityCheckEvent;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PvPServerUtils extends PvPCommonUtils
{

    private static Provider provider;

    public static boolean setProvider (Provider provider)
    {
        if (PvPServerUtils.provider == null)
        {
            PvPServerUtils.provider = provider;
            return true;
        }
        return false;
    }

    /**
     * A filter asking the compatibility modules whether the item stack *could* be dropped.
     */
    public static final Predicate<ItemStack> PARTIAL_INVENTORY_LOSS_COMP_FILTER = stack ->
    {
        return !MinecraftForge.EVENT_BUS.post (new PartialItemLossEvent (stack));
    };

    /**
     * A filter which only permits armour items.
     */
    public static final Predicate<ItemStack> ARMOUR_FILTER = stack ->
    {
        return stack.getItem () instanceof ItemArmor;
    };

    /**
     * Returns the system time in seconds.
     */
    public static long getTime ()
    {
        return MinecraftServer.getSystemTimeMillis () / 1000;
    }

    /**
     * Returns the EntityPlayerMP with the specified name.
     */
    public static EntityPlayerMP getPlayer (String name)
    {
        return provider.getPlayer (name);
    }

    /**
     * Determines whether the command sender has admin privileges.
     */
    public static boolean isOpped (ICommandSender sender)
    {
        return provider.isOpped (sender);
    }

    /**
     * Returns a wrapper from which all player-specific PvP properties can be accessed. The returned
     * instance can be returned from a cache.
     */
    public static PvPData getPvPData (EntityPlayer player)
    {
        return provider.getPvPData (player);
    }

    /**
     * Returns the PvPMode of the supplied player. If ON, the player can do PvP, otherwise not.
     */
    public static EnumPvPMode getPvPMode (EntityPlayer player)
    {
        // Creative and flying players cannot do PvP
        if (isCreativeMode (player) || canFly (player))
            return EnumPvPMode.OFF;

        PvPData data = PvPServerUtils.getPvPData (player);

        EnumForcedPvPMode forcedPvPMode = data.getForcedPvPMode ();
        if (!arePvPModeOverridesEnabled () || forcedPvPMode == EnumForcedPvPMode.UNDEFINED)
        {
            // No PvP mode overrides apply
            if (data.getPvPTimer () == 0)
                // Player is not in PvP
                return EnumPvPMode.fromBoolean (data.isPvPEnabled ());
            else // Player is in PvP
                return EnumPvPMode.ON;
        }
        else // PvP mode overrides apply
            return forcedPvPMode.toPvPMode ();
    }

    /**
     * Returns whether the supplied player is in creative mode.
     */
    public static boolean isCreativeMode (EntityPlayer player)
    {
        return player.capabilities.isCreativeMode;
    }

    /**
     * Returns whether the supplied player can fly.
     */
    public static boolean canFly (EntityPlayer player)
    {
        return player.capabilities.allowFlying;
    }

    /**
     * Returns the distance between the two supplied players rounded with the distance rounding
     * factor specified in the configuration file.
     */
    public static int roundedDistanceBetween (EntityPlayerMP sender, EntityPlayerMP player)
    {
        return provider.roundedDistanceBetween (sender, player);
    }

    /**
     * Posts the supplied event in the Forge event bus and returns a result gotten from the supplied
     * getter function.
     */
    public static <T> T postEventAndGetResult (Event event, Supplier<T> resultGetter)
    {
        MinecraftForge.EVENT_BUS.post (event);
        if (!event.isCanceled ())
            return resultGetter.get ();
        return null;
    }

    /**
     * Returns the indices of all filled slots of the supplied "inventory".<br> A filled slot is a
     * slot with an item stack in it.
     */
    public static Set<Integer> getFilledInventorySlots (ItemStack[] inventory, int startIndex,
        int endIndex)
    {
        return getFilledInventorySlots (inventory, startIndex, endIndex, null);
    }

    /**
     * Returns the indices of all filled slots of the supplied "inventory".<br> A filled slot is a
     * slot with an item stack in it. Optionally, they can be filtered with the supplied filter
     * (whitelist).
     */
    public static Set<Integer> getFilledInventorySlots (ItemStack[] inventory, int startIndex,
        int endIndex,
        Predicate<ItemStack> filter)
    {
        Set<Integer> filledSlots = new HashSet<> ();
        for (int i = startIndex; i < endIndex + 1; i++)
        {
            if (inventory[i] != null && (filter != null ? filter.test (inventory[i]) : true))
            {
                filledSlots.add (i);
            }
        }
        return filledSlots;
    }

    /**
     * Returns whether the conditional PvP mode overrides are enabled.
     */
    public static boolean arePvPModeOverridesEnabled ()
    {
        return provider.arePvPModeOverridesEnabled ();
    }

    /**
     * Returns whether the PvP mode for the supplied player is overridden.
     */
    public static boolean isPvPModeOverriddenForPlayer (EntityPlayer player)
    {
        return arePvPModeOverridesEnabled ()
            && getPvPData (player).getForcedPvPMode () != EnumForcedPvPMode.UNDEFINED;
    }

    /**
     * Returns whether the supplied player is currently in PvP.<br> If a PvP event occurred with
     * this player involved, a timer starts. While this timer is running, the player is considered
     * to be involved into PvP.
     */
    public static boolean isInPvP (EntityPlayer player)
    {
        return getPvPData (player).getPvPTimer () != 0;
    }

    /**
     * Returns whether the supplied command can be assigned to the supplied name.
     */
    public static boolean matches (ICommand command, String name)
    {
        if (command.getCommandName ().equals (name))
            return true;
        else if (command.getCommandAliases () != null)
            return command.getCommandAliases ().contains (name);
        else
            return false;
    }

    /**
     * Returns the player that this entity is associated with, if possible.
     */
    public static EntityPlayerMP getMaster (Entity entity)
    {
        if (entity == null)
            return null;

        if (entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;

            // Check whether the supplied player is a real one
            if (!MinecraftForge.EVENT_BUS.post (new PlayerIdentityCheckEvent (player)))
                return player;
        }

        List<Entity> entitiesChecked = new ArrayList<> ();
        Entity owner = entity;
        while (owner instanceof IEntityOwnable)
        {
            owner = ((IEntityOwnable) owner).getOwner ();
            if (owner == null || entitiesChecked.contains (owner))
            {
                break;
            }
            entitiesChecked.add (owner);
            if (owner instanceof EntityPlayerMP)
                return (EntityPlayerMP) owner;
        }

        // Via this event the compatibility modules will be asked to extract the master
        EntityMasterExtractionEvent event = new EntityMasterExtractionEvent (entity);
        return PvPServerUtils.postEventAndGetResult (event, event::getMaster);
    }

    /**
     * Returns whether the warmup timer for the supplied player is running
     */
    public static boolean isWarmupTimerRunning (EntityPlayer player)
    {
        return getPvPData (player).getPvPWarmup () != 0;
    }

    /**
     * Returns the remaining warmup time.
     */
    public static long getWarmupTimer (EntityPlayer player)
    {
        return PvPServerUtils.getTimer (PvPServerUtils.getPvPData (player).getPvPWarmup ());
    }

    /**
     * Returns the remaining cooldown time.
     */
    public static long getCooldownTimer (EntityPlayer player)
    {
        return PvPServerUtils.getTimer (PvPServerUtils.getPvPData (player).getPvPCooldown ());
    }

    /**
     * Returns the remaining PvP timer time.
     */
    public static long getPvPTimer (EntityPlayer player)
    {
        return PvPServerUtils.getTimer (PvPServerUtils.getPvPData (player).getPvPTimer ());
    }

    private static long getTimer (long futureTime)
    {
        return Math.max (futureTime - PvPServerUtils.getTime (), 0);
    }

    /**
     * Returns whether the supplied player cannot transfer items in his inventory via
     * shift-clicking.
     */
    public static boolean isShiftClickingBlocked (EntityPlayer player)
    {
        return provider.isShiftClickingBlocked (player);
    }

    /**
     * Sends the PvP stats of the supplied player to the recipient.
     */
    public static void displayPvPStats (ICommandSender sender, EntityPlayer displayedPlayer)
    {
        provider.displayPvPStats (sender, displayedPlayer);
    }

    public static boolean isSoulbound (ItemStack stack)
    {
        return stack.hasTagCompound () && stack.getTagCompound ().getBoolean ("SoulboundBool");
    }

    public static interface Provider
    {
        public EntityPlayerMP getPlayer (String name);

        public boolean isOpped (ICommandSender sender);

        public PvPData getPvPData (EntityPlayer player);

        public int roundedDistanceBetween (EntityPlayerMP sender, EntityPlayerMP player);

        public boolean arePvPModeOverridesEnabled ();

        public boolean isShiftClickingBlocked (EntityPlayer player);

        public void displayPvPStats (ICommandSender sender, EntityPlayer displayedPlayer);
    }

}
