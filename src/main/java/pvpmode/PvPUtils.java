package pvpmode;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.command.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.compatibility.events.*;
import pvpmode.overrides.EnumForcedPvPMode;

public class PvPUtils
{

    public static final String SOMETHING_WENT_WRONG_MESSAGE = "IF YOU SEE THIS, SOMETHING WENT WRONG. PLEASE REPORT IT.";

    /**
     * A filter asking the compatibility modules whether the item stack *could* be
     * dropped.
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

    private static final Map<UUID, PvPData> playerData = new HashMap<> ();

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
        return PvPMode.cfg.func_152612_a (name);
    }

    /**
     * Determines whether the command sender has admin privileges.
     */
    public static boolean isOpped (ICommandSender sender)
    {
        if (sender instanceof EntityPlayerMP)
            return PvPMode.cfg.func_152596_g ( ((EntityPlayerMP) sender).getGameProfile ());

        return true;
    }

    /**
     * Returns a wrapper from which all player-specific PvP properties can be
     * accessed. The returned instance can be returned from a cache.
     */
    public static PvPData getPvPData (EntityPlayer player)
    {
        if (!playerData.containsKey (player.getUniqueID ()))
        {
            playerData.put (player.getUniqueID (), new PvPData (player));
        }
        return playerData.get (player.getUniqueID ());
    }

    /**
     * Returns the PvPMode of the supplied player. If ON, the player can do PvP,
     * otherwise not.
     */
    public static EnumPvPMode getPvPMode (EntityPlayer player)
    {
        // Creative and flying players cannot do PvP
        if (isCreativeMode (player) || canFly (player))
            return EnumPvPMode.OFF;

        PvPData data = PvPUtils.getPvPData (player);

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
     * Returns the distance between the two supplied players rounded with the
     * distance rounding factor specified in the configuration file.
     */
    public static int roundedDistanceBetween (EntityPlayerMP sender, EntityPlayerMP player)
    {
        double x = sender.posX - player.posX;
        double z = sender.posZ - player.posZ;

        double distance = Math.sqrt (x * x + z * z);

        return (int) (distance / PvPMode.roundFactor + 1) * PvPMode.roundFactor;
    }

    /**
     * Posts the supplied event in the Forge event bus and returns a result gotten
     * from the supplied getter function.
     */
    public static <T> T postEventAndGetResult (Event event, Supplier<T> resultGetter)
    {
        MinecraftForge.EVENT_BUS.post (event);
        if (!event.isCanceled ())
            return resultGetter.get ();
        return null;
    }

    /**
     * Returns the indices of all filled slots of the supplied "inventory".<br/>
     * A filled slot is a slot with an item stack in it.
     */
    public static Set<Integer> getFilledInventorySlots (ItemStack[] inventory, int startIndex, int endIndex)
    {
        return getFilledInventorySlots (inventory, startIndex, endIndex, null);
    }

    /**
     * Returns the indices of all filled slots of the supplied "inventory".<br/>
     * A filled slot is a slot with an item stack in it. Optionally, they can be
     * filtered with the supplied filter (whitelist).
     */
    public static Set<Integer> getFilledInventorySlots (ItemStack[] inventory, int startIndex, int endIndex,
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
        return PvPMode.overrideCheckInterval != -1;
    }

    /**
     * Returns whether the PvP mode for the supplied player is overridden.
     */
    public static boolean isPvPModeOverriddenForPlayer (EntityPlayer player)
    {
        return arePvPModeOverridesEnabled () && getPvPData (player).getForcedPvPMode () != EnumForcedPvPMode.UNDEFINED;
    }

    /**
     * Writes the contents of the supplied stream to the specified file.<br/>
     * The file must exist on the filesystem.
     *
     * @param stream
     *            A supplier which creates the input stream
     * @param file
     *            The file where the data should be stored
     * @throws IOException
     *             If IO errors occur
     */
    public static void writeFromStreamToFile (Supplier<InputStream> stream, Path file) throws IOException
    {
        try (InputStream in = stream.get ();
            OutputStream out = Files.newOutputStream (file))
        {
            IOUtils.copy (in, out);
        }
    }

    /**
     * Returns whether the supplied player is currently in PvP.<br/>
     * If a PvP event occurred with this player involved, a timer starts. While this
     * timer is running, the player is considered to be involved into PvP.
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
        else return false;
    }

    /**
     * Returns the player that this entity is associated with, if possible.
     */
    public static EntityPlayerMP getMaster (Entity entity)
    {
        if (entity == null)
            return null;

        if (entity instanceof EntityPlayerMP)
            return (EntityPlayerMP) entity;

        if (entity instanceof IEntityOwnable)
        {
            List<Integer> entitiesChecked = new ArrayList<>();
            Entity owner;
            do {
                owner = ((IEntityOwnable) owner == null ? entity : owner).getOwner ();
                if (entitiesChecked.contains (owner.getEntityId()) break;
                entitiesChecked.add (owner.getEntityId())
                if (owner instanceof EntityPlayerMP) return (EntityPlayerMP) owner;
            } while (owner instanceof IEntityOwnable);
	}

        // Via this event the compatibility modules will be asked to extract the master
        EntityMasterExtractionEvent event = new EntityMasterExtractionEvent (entity);
        return PvPUtils.postEventAndGetResult (event, event::getMaster);
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
        return PvPUtils.getTimer (PvPUtils.getPvPData (player).getPvPWarmup ());
    }

    /**
     * Returns the remaining cooldown time.
     */
    public static long getCooldownTimer (EntityPlayer player)
    {
        return PvPUtils.getTimer (PvPUtils.getPvPData (player).getPvPCooldown ());
    }

    /**
     * Returns the remaining PvP timer time.
     */
    public static long getPvPTimer (EntityPlayer player)
    {
        return PvPUtils.getTimer (PvPUtils.getPvPData (player).getPvPTimer ());
    }

    private static long getTimer (long futureTime)
    {
        return Math.max (futureTime - PvPUtils.getTime (), 0);
    }

    /**
     * Returns whether the supplied player cannot transfer items in his inventory
     * via shift-clicking.
     */
    public static boolean isShiftClickingBlocked (EntityPlayer player)
    {
        return PvPMode.blockShiftClicking && PvPUtils.isInPvP (player);
    }

    /**
     * Returns "enabled" if the supplied boolean is true, "disabled" otherwise.
     */
    public static String getEnabledString (boolean enabled)
    {
        return enabled ? "enabled" : "disabled";
    }

    /**
     * Returns the direction of the supplied player relative to the other supplied
     * player.
     */
    public static String getPlayerDirection (EntityPlayer origin, EntityPlayer player)
    {
        double toPlayerX = player.posX - origin.posX;
        double toPlayerZ = player.posZ - origin.posZ;

        double angle = -90 - Math
            .toDegrees (Math.atan2 (toPlayerZ, toPlayerX));

        if (angle < 0)
        {
            angle += 360;
        }

        String direction = SOMETHING_WENT_WRONG_MESSAGE;

        if (angle >= 0.0 && angle <= 22.5 || angle >= 337.5 && angle <= 360.0)
        {
            direction = "N";
        }
        else if (angle > 22.5 && angle < 67.5)
        {
            direction = "NW";
        }
        else if (angle >= 67.5 && angle <= 112.5)
        {
            direction = "W";
        }
        else if (angle > 112.5 && angle < 157.5)
        {
            direction = "SW";
        }
        else if (angle >= 157.5 && angle <= 202.5)
        {
            direction = "S";
        }
        else if (angle > 202.5 && angle < 247.5)
        {
            direction = "SE";
        }
        else if (angle >= 247.5 && angle <= 292.5)
        {
            direction = "E";
        }
        else if (angle > 292.5 && angle < 337.5)
        {
            direction = "NE";
        }
        return direction;
    }

    /**
     * Sends the PvP stats of the supplied player to the recipient.
     */
    public static void displayPvPStats (ICommandSender sender, EntityPlayer displayedPlayer)
    {
        boolean isSenderDisplayed = sender == displayedPlayer;
        PvPData data = PvPUtils.getPvPData (displayedPlayer);
        EnumPvPMode pvpMode = PvPUtils.getPvPMode (displayedPlayer);
        boolean isOverridden = data.getForcedPvPMode () != EnumForcedPvPMode.UNDEFINED;
        boolean spying = data.isSpyingEnabled ();
        long warmupTimer = PvPUtils.getWarmupTimer (displayedPlayer);
        long cooldownTimer = PvPUtils.getCooldownTimer (displayedPlayer);
        long pvpTimer = PvPUtils.getPvPTimer (displayedPlayer);
        boolean defaultPvPModeForced = data.isDefaultModeForced ();

        ChatUtils.green (sender, String.format ("------ %sPvP Stats ------", isSenderDisplayed ? "Your " : ""));
        if (!isSenderDisplayed)
        {
            ChatUtils.postLocalChatMessage (sender, "For: ", displayedPlayer.getDisplayName (), EnumChatFormatting.GRAY,
                EnumChatFormatting.DARK_GREEN);
        }
        ChatUtils.postLocalChatMessage (sender, "PvP Mode: ", pvpMode.toString (), EnumChatFormatting.GRAY,
            pvpMode == EnumPvPMode.ON ? EnumChatFormatting.RED : EnumChatFormatting.GREEN);
        ChatUtils.postLocalChatMessage (sender, "Is Overridden: ", Boolean.toString (isOverridden),
            EnumChatFormatting.GRAY, EnumChatFormatting.WHITE);
        if (PvPMode.allowPerPlayerSpying && PvPMode.radar)
        {
            ChatUtils.postLocalChatMessage (sender, "Spying Enabled: ", Boolean.toString (spying),
                EnumChatFormatting.GRAY,
                EnumChatFormatting.WHITE);
        }
        ChatUtils.postLocalChatMessage (sender, "Warmup Timer: ", Long.toString (warmupTimer) + "s",
            EnumChatFormatting.GRAY, warmupTimer == 0 ? EnumChatFormatting.WHITE : EnumChatFormatting.YELLOW);
        ChatUtils.postLocalChatMessage (sender, "Cooldown Timer: ", Long.toString (cooldownTimer) + "s",
            EnumChatFormatting.GRAY, cooldownTimer == 0 ? EnumChatFormatting.WHITE : EnumChatFormatting.YELLOW);
        ChatUtils.postLocalChatMessage (sender, "PvP Timer: ", Long.toString (pvpTimer) + "s", EnumChatFormatting.GRAY,
            pvpTimer == 0 ? EnumChatFormatting.WHITE : EnumChatFormatting.YELLOW);
        if (PvPMode.forceDefaultPvPMode && !PvPMode.pvpTogglingEnabled && !isOverridden)
        {
            ChatUtils.postLocalChatMessage (sender, "Default PvP Mode Forced: ",
                Boolean.toString (defaultPvPModeForced),
                EnumChatFormatting.GRAY,
                EnumChatFormatting.WHITE);
        }
        ChatUtils.green (sender, StringUtils.repeat ('-', isSenderDisplayed ? 26 : 21));
    }
}
