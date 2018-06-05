package pvpmode.command;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
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
    public int getRequiredPermissionLevel ()
    {
        return 2;
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

        if (player == null)
        {
            PvPUtils.red (admin, String.format ("The player \"%s\" doesn't exist", args[0]));
            return;
        }

        /*
         * This warning will never be a config option. I will not tolerate
         * admins who go behind a player's back as "punishment" An admin should
         * be able to keep order on a server without resorting to deception and
         * secrecy.
         */
        warnPlayer (player);
        PvPUtils.getPvPData (player).setPvPWarmup (PvPUtils.getTime ());
    }

    @Override
    public boolean isUsernameIndex (String[] args, int index)
    {
        return index == 0;
    }

    public List<?> addTabCompletionOptions (ICommandSender sender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord (args, MinecraftServer.getServer ().getAllUsernames ());

        return null;
    }

    void help (ICommandSender sender)
    {
        PvPUtils.white (sender, "/pvpadmin <player>");
    }

    void warnPlayer (EntityPlayerMP player)
    {
        PvPUtils.red (player, "WARNING: Your PvP status is being overridden by an admin.");
    }
}
