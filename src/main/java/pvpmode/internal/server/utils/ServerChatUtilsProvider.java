package pvpmode.internal.server.utils;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.*;
import pvpmode.PvPMode;
import pvpmode.api.server.utils.ServerChatUtils;
import pvpmode.internal.server.ServerProxy;

public class ServerChatUtilsProvider implements ServerChatUtils.Provider
{

    private final ServerProxy server;

    public ServerChatUtilsProvider (ServerProxy server)
    {
        this.server = server;
    }

    @Override
    public void postLocalChatMessages (ICommandSender recipient, EnumChatFormatting color, String... messages)
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

    @Override
    public void postLocalChatMessage (ICommandSender recipient, String firstPart, String secondPart,
        EnumChatFormatting firstColor, EnumChatFormatting secondColor)
    {
        ChatComponentText firstText = new ChatComponentText (firstPart);
        ChatComponentText secondText = new ChatComponentText (secondPart);

        firstText.getChatStyle ().setColor (firstColor);
        secondText.getChatStyle ().setColor (secondColor);

        recipient.addChatMessage (firstText.appendSibling (secondText));
    }

    @Override
    public void postGlobalChatMessages (EnumChatFormatting color, String... messages)
    {
        for (String message : messages)
        {
            for (String line : message.split ("\n"))
            {
                ChatComponentText prefix = new ChatComponentText (
                    server.isPrefixGlobalMessages () ? server.getGlobalMessagePrefix () : "");
                ChatComponentText text = new ChatComponentText (line);
                text.getChatStyle ().setColor (color);
                PvPMode.instance.getServerProxy ().getServerConfigurationManager ()
                    .sendChatMsg (prefix.appendSibling (text));
            }
        }
    }

}
