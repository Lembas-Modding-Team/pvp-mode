package pvpmode.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import pvpmode.PvPMode;

public class PvPCommandHelp extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "pvphelp";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/pvphelp";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        sender.addChatMessage (new ChatComponentText (
                        "/pvp: Starts a warmup timer to enable or disable PvP for the command sender."));
        sender.addChatMessage (new ChatComponentText (
                        "/pvpcancel: Cancels the warmup timer for the command sender."));
        sender.addChatMessage (new ChatComponentText (
                        "/pvpadmin <player>: For admins only, enables or disables PvP for the player."));
        sender.addChatMessage (new ChatComponentText (
                        "/pvplist: Displays a list of all players on the server, their PvP modes,"
                                        + " and if hostile, their approximate distance to the command sender."));
        sender.addChatMessage (new ChatComponentText ("Default cooldown: "
                        + PvPMode.cooldown + "s, default warmup: " + PvPMode.warmup
                        + "s, pvplist distances enabled: " + PvPMode.radar));
    }
}
