package pvpmode.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import pvpmode.PvPUtils;

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
        NBTTagCompound data = PvPUtils.getPvPData (player);
        long warmup = data.getLong ("PvPWarmup");

        if (warmup == 0)
            PvPUtils.yellow (sender, "No PvP warmup to cancel.");
        else
        {
            data.setLong ("PvPWarmup", 0);
            PvPUtils.yellow (sender, "PvP warmup canceled.");
        }
    }
}
