package pvpmode.command;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.entity.player.*;
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
        return "/pvpadmin <player> [on|off]";
    }

    @Override
    public int getRequiredPermissionLevel ()
    {
        return 2;
    }

    @Override
    public void processCommand (ICommandSender admin, String[] args)
    {

        requireMinLength (admin, args, 1);

        EntityPlayerMP player = CommandBase.getPlayer (admin, args[0]);

        PvPData data = PvPUtils.getPvPData (player);

        this.requireNonCreativePlayer (player);
        this.requireNonFlyingPlayer (player);
        this.requireNonOverriddenPlayer (player);
        this.requireNonPvPPlayer (player);

        if (args.length > 1)
        {
            switch (this.requireArguments (admin, args, 1, "on", "off"))
            {
                case "off":
                    togglePvPMode (admin, player, data, Boolean.FALSE);
                    break;
                case "on":
                    togglePvPMode (admin, player, data, Boolean.TRUE);
                    break;
            }
        }
        else
        {
            togglePvPMode (admin, player, data, null);
        }
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

    private void togglePvPMode (ICommandSender sender, EntityPlayer player, PvPData data, Boolean mode)
    {

        if (mode == null ? true : mode.booleanValue () != data.isPvPEnabled ())
        {
            /*
             * This warning will never be a config option. I will not tolerate admins who go
             * behind a player's back as "punishment" An admin should be able to keep order
             * on a server without resorting to deception and secrecy.
             */
            PvPUtils.red (player, "Your PvP mode is being toggled by an admin");
            data.setPvPWarmup (PvPUtils.getTime ());

            PvPUtils.green (sender,
                String.format ("PvP is now %s for %s", PvPUtils.getEnabledString (!data.isPvPEnabled ()),
                    player.getDisplayName ()));
        }
        else
        {
            PvPUtils.yellow (sender,
                String.format ("PvP is already %s for %s", PvPUtils.getEnabledString (mode.booleanValue ()),
                    player.getDisplayName ()));
        }
    }

}
