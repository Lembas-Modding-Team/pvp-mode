package pvpmode.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.*;
import pvpmode.*;

public class PvPCommandHelp extends AbstractPvPCommand
{
    @Override
    public String getCommandName ()
    {
        return "pvphelp";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return "/pvphelp";
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        PvPUtils.green (sender, "--- PvP Mode Help ---");
        postCommandHelp (sender, "pvp", "", "Starts a warmup timer to enable or disable PvP for the command sender.");
        postCommandHelp (sender, "pvp cancel", "", "Cancels the warmup timer for the command sender.");
        if (PvPMode.allowPerPlayerSpying)
            postCommandHelp (sender, "pvp spy ", "[on|off]", "Allows players to toggle their spying settings.");
        postCommandHelp (sender, "pvpadmin ", "<player>", "For admins only, enables or disables PvP for the player.");
        postCommandHelp (sender, "pvplist", "",
            "Displays a list of all players on the server, their PvP modes, and if hostile, their approximate distance to the command sender.");
        postCommandHelp (sender, "pvpconfig display", "", "For admins only, displays the server configuration.");
        PvPUtils.green (sender, "-------------------");
    }

    private void postCommandHelp (ICommandSender sender, String commandName, String commandUsage, String help)
    {
        ChatComponentText commandPart = new ChatComponentText (
            ("/" + commandName.trim () + " " + commandUsage).trim ());
        commandPart.getChatStyle ().setChatClickEvent (new ClickEvent (Action.SUGGEST_COMMAND, "/" + commandName))
            .setColor (EnumChatFormatting.GRAY);
        ChatComponentText helpPart = new ChatComponentText (": " + help);
        helpPart.getChatStyle ().setColor (EnumChatFormatting.WHITE);
        sender.addChatMessage (commandPart.appendSibling (helpPart));
    }
}
