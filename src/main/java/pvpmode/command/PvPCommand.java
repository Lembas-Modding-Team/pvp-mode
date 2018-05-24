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
        PvPData data = PvPUtils.getPvPData (player);

        if (args.length > 0)
        {
            if (args[0].equals ("cancel"))
            {
                cancelPvPTimer (sender, data);
            }
            else
            {
                PvPUtils.red (sender, getCommandUsage (sender));
            }
        }
        else
        {
            togglePvPMode (sender, data);
        }
    }

    private void cancelPvPTimer (ICommandSender sender, PvPData data)
    {
        long warmup = data.getPvPWarmup ();
        if (warmup == 0)
            PvPUtils.yellow (sender, "No PvP warmup to cancel.");
        else
        {
            data.setPvPWarmup (0);
            PvPUtils.yellow (sender, "PvP warmup canceled.");
        }
    }

    private void togglePvPMode (ICommandSender sender, PvPData data)
    {
        long time = PvPUtils.getTime ();
        long toggleTime = time + PvPMode.warmup;
        long cooldownTime = data.getPvPCooldown ();

        if (cooldownTime > time)
        {
            long wait = cooldownTime - time;
            PvPUtils.yellow (sender, String.format ("Please wait %d seconds before issuing this command.", wait));
            return;
        }

        data.setPvPWarmup (toggleTime);
        data.setPvPCooldown (0);

        String status = data.isPvPEnabled () ? "disabled" : "enabled";
        PvPUtils.yellow (sender, String.format ("PvP will be %s in %d seconds...", status, PvPMode.warmup));
    }
}
