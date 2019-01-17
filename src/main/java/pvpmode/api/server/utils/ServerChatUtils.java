package pvpmode.api.server.utils;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class ServerChatUtils
{

    public static final String DEFAULT_CHAT_MESSAGE_PREFIX = "\u00A74[PvP Mode]: ";

    private static Provider provider;

    public static final boolean setProvider (Provider provider)
    {
        if (ServerChatUtils.provider == null)
        {
            ServerChatUtils.provider = provider;
            return true;
        }
        return false;
    }

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
        provider.postLocalChatMessages (recipient, color, messages);
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
     * Displays the specified message to the recipient. Each part of the message can
     * have it's own color.
     */
    public static void postLocalChatMessage (ICommandSender recipient, String firstPart, String secondPart,
        EnumChatFormatting firstColor, EnumChatFormatting secondColor)
    {
        provider.postLocalChatMessage (recipient, firstPart, secondPart, firstColor, secondColor);
    }

    /**
     * Displays the specified messages to every player on the server with the
     * specified color. Each entry of the messages array will be displayed as a new
     * line in the chat.
     */
    public static void postGlobalChatMessages (EnumChatFormatting color,
        String... messages)
    {
        provider.postGlobalChatMessages (color, messages);
    }

    /**
     * Displays the specified messages to every player on the server. Each entry of
     * the messages array will be displayed as a new line in the chat.
     */
    public static void postGlobalChatMessages (String... messages)
    {
        postGlobalChatMessages (null, messages);
    }

    public static interface Provider
    {

        public void postLocalChatMessages (ICommandSender recipient, EnumChatFormatting color,
            String... messages);

        public void postLocalChatMessage (ICommandSender recipient, String firstPart, String secondPart,
            EnumChatFormatting firstColor, EnumChatFormatting secondColor);

        public void postGlobalChatMessages (EnumChatFormatting color,
            String... messages);

    }
}
