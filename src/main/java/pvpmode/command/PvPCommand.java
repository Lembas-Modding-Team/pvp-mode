package pvpmode.command;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import pvpmode.*;

public class PvPCommand extends CommandBase
{
    @Override
    public String getCommandName ()
    {
        return "pvp";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return "/pvp [cancel]";
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        EntityPlayerMP player = getCommandSenderAsPlayer (sender);
        PvpData data = PvPUtils.getPvPData (player);

        if (args.length > 0)
        {
            if (args[0].equals ("cancel"))
            {
                cancelPvpTimer (sender, data);
            }
            else
            {
                PvPUtils.red (sender, getCommandUsage (sender));
            }
        }
        else
        {
            togglePvpMode (sender, data);
        }
    }

    private void cancelPvpTimer (ICommandSender sender, PvpData data)
    {
        long warmup = data.getPvpWarmup ();
        if (warmup == 0)
            PvPUtils.yellow (sender, "No PvP warmup to cancel.");
        else
        {
            data.setPvpWarmup (0);
            PvPUtils.yellow (sender, "PvP warmup canceled.");
        }
    }

    private void togglePvpMode (ICommandSender sender, PvpData data)
    {
        long time = PvPUtils.getTime ();
        long toggleTime = time + PvPMode.warmup;
        long cooldownTime = data.getPvpCooldown ();

        if (cooldownTime > time)
        {
            long wait = cooldownTime - time;
            PvPUtils.yellow (sender, String.format ("Please wait %d seconds before issuing this command.", wait));
            return;
        }

        data.setPvpWarmup (toggleTime);
        data.setPvpCooldown (0);

        String status = data.isPvpEnabled () ? "disabled" : "enabled";
        PvPUtils.yellow (sender, String.format ("PvP will be %s in %d seconds...", status, PvPMode.warmup));
    }
}
