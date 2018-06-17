package pvpmode;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.overrides.EnumForcedPvPMode;

public class PvPUtils
{

    public static final String SOMETHING_WENT_WRONG_MESSAGE = "IF YOU SEE THIS, SOMETHING WENT WRONG. PLEASE REPORT IT.";

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
        return new PvPData (player);
    }

    /**
     * Displays the specified messages to the recipient in yellow.
     */
    public static void yellow (ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, EnumChatFormatting.YELLOW, messages);
    }

    /**
     * Displays the specified messages to the recipient in red.
     */
    public static void red (ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, EnumChatFormatting.RED, messages);
    }

    /**
     * Displays the specified messages to the recipient in green.
     */
    public static void green (ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, EnumChatFormatting.GREEN, messages);
    }

    /**
     * Displays the specified messages to the recipient in blue.
     */
    public static void blue (ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, EnumChatFormatting.BLUE, messages);
    }

    /**
     * Displays the specified messages to the recipient in white.
     */
    public static void white (ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, EnumChatFormatting.WHITE, messages);
    }

    /**
     * Displays the specified messages to the recipient with the specified
     * formatting. Each entry of the messages array will be displayed as a new
     * line in the chat.
     */
    public static void postChatLines (ICommandSender recipient, EnumChatFormatting formatting, String... messages)
    {
        for (String message : messages)
        {
            ChatComponentText text = new ChatComponentText (message);
            text.getChatStyle ().setColor (formatting);
            recipient.addChatMessage (text);
        }
    }

    /**
     * Displays the specified messages to the recipient. Each entry of the
     * messages array will be displayed as a new line in the chat.
     */
    public static void postChatLines (ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, null, messages);
    }

    /**
     * Returns the PvPMode of the supplied player. If ON, the player can do PvP,
     * otherwise not.
     */
    public static EnumPvPMode getPvPMode (EntityPlayer player)
    {
        if (isCreativeMode (player) || canFly (player))
            return EnumPvPMode.OFF;// This is not really my (CraftedMods) style,
                                   // but I'm doing this for performance reasons
                                   // here, because the PvPData will only be
                                   // loaded if required.

        PvPData data = PvPUtils.getPvPData (player);
        EnumForcedPvPMode forcedPvPMode = data.getForcedPvPMode ();
        return forcedPvPMode == EnumForcedPvPMode.UNDEFINED || !arePvPModeOverridesEnabled ()
            ? data.getPvPWarmup () == 0 ? data.isPvPEnabled () ? EnumPvPMode.ON : EnumPvPMode.OFF
                : EnumPvPMode.WARMUP
            : forcedPvPMode.toPvPMode ();
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

        return (int) ( (distance) / PvPMode.roundFactor + 1) * PvPMode.roundFactor;
    }

    /**
     * Posts the supplied event in the Forge event bus and returns a result
     * gotten from the supplied getter function.
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
        Set<Integer> filledSlots = new HashSet<> ();
        for (int i = startIndex; i < endIndex + 1; i++)
        {
            if (inventory[i] != null)
                filledSlots.add (i);
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
     * Returns whether the PvP mode for the player whose data are supplied is
     * overridden.
     */
    public static boolean isPvPModeOverriddenForPlayer (PvPData data)
    {
        return arePvPModeOverridesEnabled () && data.getForcedPvPMode () != EnumForcedPvPMode.UNDEFINED;
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
            InputStreamReader bridge = new InputStreamReader (in);
            BufferedReader reader = new BufferedReader (bridge);
            BufferedWriter writer = Files.newBufferedWriter (file))
        {
            String line = null;
            while ( (line = reader.readLine ()) != null)
            {
                writer.write (line);
                writer.newLine ();
            }
        }
    }

}
