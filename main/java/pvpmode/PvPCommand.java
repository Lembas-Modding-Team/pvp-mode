package pvpmode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

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
        return "/pvp";
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        if (args.length > 0)
        {
            help (sender);
            return;
        }

        EntityPlayerMP player = getCommandSenderAsPlayer (sender);
        long time = PvPUtils.getTime ();
        long toggleTime = time + PvPMode.warmup;
        long cooldownTime = player.getEntityData ().getLong ("PvPCooldown");

        if (cooldownTime > time)
        {
            long wait = cooldownTime - time;
            waitCooldown (sender, wait);
            return;
        }

        player.getEntityData ().setLong ("PvPWarmup", toggleTime);
        player.getEntityData ().setLong ("PvPCooldown", 0);
        waitWarmup (player);
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    void help (ICommandSender sender)
    {
        sender.addChatMessage (new ChatComponentText ("/pvp"));
    }

    void waitWarmup (EntityPlayerMP sender)
    {
        String status = sender.getEntityData ().getBoolean ("PvPEnabled") ? "disabled" : "enabled";

        sender.addChatMessage (new ChatComponentText (EnumChatFormatting.YELLOW
            + "PvP will be " + status + " in " + PvPMode.warmup + " seconds..."));
    }

    void waitCooldown (ICommandSender sender, long wait)
    {
        sender.addChatMessage (new ChatComponentText (EnumChatFormatting.YELLOW +
            "Please wait " + wait + " seconds before issuing this command."));
    }
}
