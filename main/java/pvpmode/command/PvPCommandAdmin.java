package pvpmode.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import pvpmode.PvPUtils;

public class PvPCommandAdmin extends CommandBase
{
    @Override
    public String getCommandName ()
    {
        return "pvpadmin";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return "/pvpadmin <player>";
    }

    @Override
    public void processCommand (ICommandSender admin, String[] args)
    {
        if (args.length != 1)
        {
            help (admin);
            return;
        }

        EntityPlayerMP player = PvPUtils.getPlayer (args[0]);

        /*
         * This warning will never be a config option. I will not tolerate
         * admins who go behind a player's back as "punishment" An admin should
         * be able to keep order on a server without resorting to deception and
         * secrecy.
         */
        warnPlayer (player);
        player.getEntityData ().setLong ("PvPWarmup", PvPUtils.getTime ());
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return PvPUtils.isOpped (sender);
    }

    @Override
    public boolean isUsernameIndex (String[] args, int index)
    {
        return index == 0;
    }

    void help (ICommandSender sender)
    {
        sender.addChatMessage (new ChatComponentText ("/pvpadmin <player>"));
    }

    void warnPlayer (EntityPlayerMP player)
    {
        player.addChatMessage (new ChatComponentText (EnumChatFormatting.RED
            + "WARNING: Your PvP status is being overridden by an admin."));
    }
}
