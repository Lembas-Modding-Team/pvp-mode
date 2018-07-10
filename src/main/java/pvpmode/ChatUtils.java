package pvpmode;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.*;

public class ChatUtils
{

    public static final String DEFAULT_CHAT_MESSAGE_PREFIX = "\u00A74[PvP Mode]: ";

    /**
     * Displays the specified messages to the recipient in yellow.
     */
    public static void yellow (ICommandSender recipient, String... messages)
    {
        postLocalChatMessages (recipient, EnumChatFormatting.YELLOW, messages);
    }

    /**
     * Displays the specified messages to the recipient in red.
     */
    public static void red (ICommandSender recipient, String... messages)
    {
        postLocalChatMessages (recipient, EnumChatFormatting.RED, messages);
    }

    /**
     * Displays the specified messages to the recipient in green.
     */
    public static void green (ICommandSender recipient, String... messages)
    {
        postLocalChatMessages (recipient, EnumChatFormatting.GREEN, messages);
    }

    /**
     * Displays the specified messages to the recipient in blue.
     */
    public static void blue (ICommandSender recipient, String... messages)
    {
        postLocalChatMessages (recipient, EnumChatFormatting.BLUE, messages);
    }

    /**
     * Displays the specified messages to the recipient in white.
     */
    public static void white (ICommandSender recipient, String... messages)
    {
        postLocalChatMessages (recipient, EnumChatFormatting.WHITE, messages);
    }

    /**
     * Displays the specified messages to the recipient with the specified color.
     * Each entry of the messages array will be displayed as a new line in the chat.
     */
    public static void postLocalChatMessages (ICommandSender recipient, EnumChatFormatting color,
        String... messages)
    {
        for (String message : messages)
        {
            for (String line : message.split ("\n"))
            {
                ChatComponentText text = new ChatComponentText (line);
                text.getChatStyle ().setColor (color);
                recipient.addChatMessage (text);
            }
        }
    }

    /**
     * Displays the specified messages to the recipient. Each entry of the messages
     * array will be displayed as a new line in the chat.
     */
    public static void postLocalChatMessages (ICommandSender recipient, String... messages)
    {
        postLocalChatMessages (recipient, null, messages);
    }

    /**
     * Displays the specified messages to every player on the server with the
     * specified color. Each entry of the messages array will be displayed as a new
     * line in the chat.
     */
    public static void postGlobalChatMessages (EnumChatFormatting color,
        String... messages)
    {
        for (String message : messages)
        {
            for (String line : message.split ("\n"))
            {
                ChatComponentText prefix = new ChatComponentText (
                PvPMode.prefixGlobalMessages ? PvPMode.globalMessagePrefix : "");
                ChatComponentText text = new ChatComponentText (line);
                text.getChatStyle ().setColor (color);
                PvPMode.cfg.sendChatMsg (prefix.appendSibling (text));
            }
        }
    }

    /**
     * Displays the specified messages to every player on the server. Each entry of
     * the messages array will be displayed as a new line in the chat.
     */
    public static void postGlobalChatMessages (String... messages)
    {
        postGlobalChatMessages (null, messages);
    }

}
