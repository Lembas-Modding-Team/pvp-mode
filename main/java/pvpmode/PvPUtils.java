package pvpmode;

import cpw.mods.fml.common.Loader;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;

public class PvPUtils
{
    private static Boolean lotrmodLoaded;

    /**
     * Returns whether or not the LOTRMod is loaded
     */
    public static boolean isLOTRModLoaded()
    {
        if (lotrmodLoaded == null)
            lotrmodLoaded = Loader.isModLoaded ("lotr");
        return lotrmodLoaded.booleanValue ();
    }

    /**
     * Returns the system time in seconds.
     */
    public static long getTime()
    {
        return MinecraftServer.getSystemTimeMillis () / 1000;
    }

    /**
     * Returns the EntityPlayerMP with the specified name.
     */
    public static EntityPlayerMP getPlayer(String name)
    {
        return PvPMode.cfg.func_152612_a (name);
    }

    /**
     * Determines whether the command sender has admin privileges.
     */
    public static boolean isOpped(ICommandSender sender)
    {
        if (sender instanceof EntityPlayerMP)
            return PvPMode.cfg.func_152596_g ( ((EntityPlayerMP) sender).getGameProfile ());

        return true;
    }

    /**
     * Returns a wrapper from which all player-specific PvP properties can be
     * accessed. The returned instance can be returned from a cache.
     */
    public static PvpData getPvPData(EntityPlayer player)
    {
       return new PvpData (player);
    }

    /**
     * Displays the specified messages to the recipient in yellow.
     */
    public static void yellow(ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, EnumChatFormatting.YELLOW, messages);
    }

    /**
     * Displays the specified messages to the recipient in red.
     */
    public static void red(ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, EnumChatFormatting.RED, messages);
    }

    /**
     * Displays the specified messages to the recipient in green.
     */
    public static void green(ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, EnumChatFormatting.GREEN, messages);
    }

    /**
     * Displays the specified messages to the recipient in white.
     */
    public static void white(ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, EnumChatFormatting.WHITE, messages);
    }

    /**
     * Displays the specified messages to the recipient with the specified
     * formatting. Each entry of the messages array will be displayed as a new
     * line in the chat.
     */
    public static void postChatLines(ICommandSender recipient, EnumChatFormatting formatting, String... messages)
    {
        for (String message : messages)
        {
            ChatComponentText text = new ChatComponentText ( (formatting == null ? "" : formatting) + message);
            recipient.addChatMessage (text);
        }
    }

    /**
     * Displays the specified messages to the recipient. Each entry of the
     * messages array will be displayed as a new line in the chat.
     */
    public static void postChatLines(ICommandSender recipient, String... messages)
    {
        postChatLines (recipient, null, messages);
    }
}
