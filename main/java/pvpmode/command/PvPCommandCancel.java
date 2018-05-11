package pvpmode.command;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import pvpmode.*;

public class PvPCommandCancel extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "pvpcancel";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/pvpcancel";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        EntityPlayerMP player = getCommandSenderAsPlayer (sender);
        PvpData data = PvPUtils.getPvPData (player);
        long warmup = data.getPvpWarmup ();

        if (warmup == 0)
            PvPUtils.yellow (sender, "No PvP warmup to cancel.");
        else
        {
            data.setPvpWarmup (0);
            PvPUtils.yellow (sender, "PvP warmup canceled.");
        }
    }
}
