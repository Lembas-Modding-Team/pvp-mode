package pvpmode.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import pvpmode.PvPUtils;

public class PvPCommandCancel extends CommandBase
{
    @Override
    public String getCommandName ()
    {
        return "pvpcancel";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return "/pvpcancel";
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        if (args.length != 0)
        {
            help (sender);
            return;
        }

        EntityPlayerMP player = getCommandSenderAsPlayer (sender);
        NBTTagCompound data = PvPUtils.getPvPData (player);
        long warmup = data.getLong ("PvPWarmup");

        if (warmup == 0)
            noWarmup (sender);
        else
        {
            data.setLong ("PvPWarmup", 0);
            canceled (player);
        }
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    void help (ICommandSender sender)
    {
        sender.addChatMessage (new ChatComponentText ("/pvpcancel"));
    }

    void noWarmup (ICommandSender sender)
    {
        sender.addChatMessage (new ChatComponentText (EnumChatFormatting.YELLOW
            + "No PvP warmup to cancel."));
    }

    void canceled (ICommandSender sender)
    {
        sender.addChatMessage (new ChatComponentText (EnumChatFormatting.YELLOW
            + "PvP warmup canceled."));
    }
}
