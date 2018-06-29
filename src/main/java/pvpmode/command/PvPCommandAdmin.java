package pvpmode.command;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import pvpmode.*;

public class PvPCommandAdmin extends AbstractPvPCommand
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

        requireArgumentLength (admin, args, 1);

        EntityPlayerMP player = CommandBase.getPlayer (admin, args[0]);

        PvPData data = PvPUtils.getPvPData (player);

        this.requireNonCreativePlayer (player);
        this.requireNonFlyingPlayer (player);
        this.requireNonOverriddenPlayer (player);
        this.requireNonPvPPlayer (player);
        
        /*
         * This warning will never be a config option. I will not tolerate admins who go
         * behind a player's back as "punishment" An admin should be able to keep order
         * on a server without resorting to deception and secrecy.
         */
        PvPUtils.red (player, "WARNING: Your PvP status is being overridden by an admin.");
        data.setPvPWarmup (PvPUtils.getTime ());
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

}
