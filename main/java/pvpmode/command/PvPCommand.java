package pvpmode.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import pvpmode.PvPMode;
import pvpmode.PvPUtils;

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
        NBTTagCompound data = PvPUtils.getPvPData (player);

        long time = PvPUtils.getTime ();
        long toggleTime = time + PvPMode.warmup;
        long cooldownTime = data.getLong ("PvPCooldown");

        String message;

        if (cooldownTime > time)
        {
            long wait = cooldownTime - time;
            message = "Please wait " + wait + " seconds before issuing this command.";
            PvPUtils.yellow (sender, message);
            return;
        }

        data.setLong ("PvPWarmup", toggleTime);
        data.setLong ("PvPCooldown", 0);

        String status = data.getBoolean ("PvPEnabled") ? "disabled" : "enabled";
        message = "PvP will be " + status + " in " + PvPMode.warmup + " seconds...";
        PvPUtils.yellow (sender, message);
    }
}
