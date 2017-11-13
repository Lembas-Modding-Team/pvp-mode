package pvpmode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class PvPCommand extends CommandBase
{
    // Holds the default warmup time for using the command on oneself.
    final long defaultWarmup = 5000;
    ChatComponentText badperm = new ChatComponentText (EnumChatFormatting.RED
        + "You do not have permission to toggle another player's PvP mode!");
    ChatComponentText help = new ChatComponentText (EnumChatFormatting.RED
        + "/pvp [player]");
    ChatComponentText wait = new ChatComponentText (EnumChatFormatting.YELLOW
        + "Wait " + defaultWarmup / 1000 + " seconds...");

    @Override
    public String getCommandName ()
    {
        return "pvp";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return help.getUnformattedText ();
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        EntityPlayerMP target;
        ServerConfigurationManager cfg = MinecraftServer.getServer ().getConfigurationManager ();

        // Admins can switch other players instantly,
        // but there is usually a five second warmup to prevent "PvP toggling"
        long warmup = 0;

        if (args.length == 0)
        {
            target = getCommandSenderAsPlayer (sender);
            warmup = defaultWarmup; // 5000 millisecond warmup.
        }
        else if (args.length == 1) // Admin-only command.
        {
            // func_152596_g determines if the player has op privileges (SP or
            // opped)
            // func_152612_a returns an EPMP from his/her name.

            if (cfg.func_152596_g (getCommandSenderAsPlayer (sender).getGameProfile ()))
                target = cfg.func_152612_a (args[0]);

            else // Command sender is not opped.
            {
                sender.addChatMessage (badperm);
                return;
            }

        }
        else // The command is incorrectly formatted.
        {
            sender.addChatMessage (help);
            return;
        }

        // PvPTime stores the system time at which the PvPDenied tag should be
        // toggled.
        target.getEntityData ().setLong ("PvPTime", MinecraftServer.getSystemTimeMillis () + warmup);

        if (warmup > 0)
            sender.addChatMessage (wait);
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    @Override
    public boolean isUsernameIndex (String[] args, int index)
    {
        // Allows for tabbing in the command.
        return index == 0;
    }
}
