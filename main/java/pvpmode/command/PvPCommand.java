package pvpmode.command;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import pvpmode.*;

public class PvPCommand extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "pvp";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/pvp";
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

        long time = PvPUtils.getTime ();
        long toggleTime = time + PvPMode.warmup;
        long cooldownTime = data.getPvpCooldown ();

        String message;

        if (cooldownTime > time)
        {
            long wait = cooldownTime - time;
            message = "Please wait " + wait + " seconds before issuing this command.";
            PvPUtils.yellow (sender, message);
            return;
        }

        data.setPvpWarmup (toggleTime);
        data.setPvpCooldown (0);

        String status = data.isPvpEnabled () ? "disabled" : "enabled";
        message = "PvP will be " + status + " in " + PvPMode.warmup + " seconds...";
        PvPUtils.yellow (sender, message);
    }
}
