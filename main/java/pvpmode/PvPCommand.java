package pvpmode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
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
        return "/pvp [player]";
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        EntityPlayerMP target;
        long time = getTime ();
        long toggleTime;

        if (args.length == 0)
        {
            target = getCommandSenderAsPlayer (sender);

            long cooldownTime = target.getEntityData ().getLong ("PvPCooldown");

            if (cooldownTime > time)
            {
                long wait = cooldownTime - time;
                waitCooldown (sender, wait);
                return;
            }

            target.getEntityData ().setLong ("PvPCooldown", 0);
            toggleTime = time + PvPMode.warmup;
        }
        else if (args.length == 1)
        {
            if (isOpped (sender))
            {
                target = getPlayer (args[0]);
                toggleTime = time;
            }
            else
            {
                badPermission (sender);
                return;
            }
        }
        else
        {
            help (sender);
            return;
        }

        target.getEntityData ().setLong ("PvPWarmup", toggleTime);

        if (toggleTime > 0)
            wait (sender);
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    @Override
    public boolean isUsernameIndex (String[] args, int index)
    {
        return index == 0;
    }

    long getTime ()
    {
        return MinecraftServer.getSystemTimeMillis () / 1000;
    }

    boolean isOpped (ICommandSender sender)
    {
        return PvPMode.cfg.func_152596_g (getCommandSenderAsPlayer (sender).getGameProfile ());
    }

    EntityPlayerMP getPlayer (String name)
    {
        return PvPMode.cfg.func_152612_a (name);
    }

    void badPermission (ICommandSender sender)
    {
        sender.addChatMessage (new ChatComponentText (EnumChatFormatting.RED
            + "You do not have permission to toggle another player's PvP mode!"));
    }

    void help (ICommandSender sender)
    {
        sender.addChatMessage (new ChatComponentText (EnumChatFormatting.RED
            + "/pvp [player]"));
    }

    void wait (ICommandSender sender)
    {
        sender.addChatMessage (new ChatComponentText (EnumChatFormatting.YELLOW
            + "Wait " + PvPMode.warmup + " seconds..."));
    }

    void waitCooldown (ICommandSender sender, long wait)
    {
        sender.addChatMessage (new ChatComponentText (EnumChatFormatting.YELLOW +
            "Please wait " + wait + " seconds..."));
    }
}
