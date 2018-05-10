package pvpmode.command;

import net.minecraft.command.*;
import net.minecraft.util.EnumChatFormatting;
import pvpmode.*;

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
        postCommandHelp (sender, "pvp", "Starts a warmup timer to enable or disable PvP for the command sender.");
        postCommandHelp (sender, "pvpcancel", "Cancels the warmup timer for the command sender.");
        postCommandHelp (sender, "pvpadmin <player>", "For admins only, enables or disables PvP for the player.");
        postCommandHelp (sender, "pvplist",
                        " Displays a list of all players on the server, their PvP modes, and if hostile, their approximate distance to the command sender.");
        PvPUtils.white (sender,
                        "Default cooldown: "
                                        + PvPMode.cooldown + "s, default warmup: " + PvPMode.warmup
                                        + "s, pvplist distances enabled: " + PvPMode.radar);
    }

    private void postCommandHelp(ICommandSender sender, String commandName, String help)
    {
        PvPUtils.postChatLines (sender, EnumChatFormatting.GRAY + "/" + commandName + EnumChatFormatting.WHITE
                        + ": " + help);
    }
}
