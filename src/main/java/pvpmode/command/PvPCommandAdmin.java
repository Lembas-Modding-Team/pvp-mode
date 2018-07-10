package pvpmode.command;

import java.util.*;

import org.apache.commons.lang3.tuple.Triple;

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
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        return Arrays.asList (Triple.of ("pvpadmin ", "<player> [on|off]", "Toggles PvP for another player."));
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        return Arrays.asList (Triple.of ("pvpadmin ", "<player> [on|off]",
            "Either toggle or set the PvP mode of another player to a specified mode (ON or OFF). The player will be informed about that."));
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "For operators. Allows them, to manage the PvP mode (ON or OFF) of another player. That player mustn't be able to fly, not in creative mode, not in PvP combat, and the PvP mode of that player mustn't be overridden.";
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
            ChatUtils.red (player, "Your PvP mode is being toggled by an admin");
            data.setPvPWarmup (PvPUtils.getTime ());

            ChatUtils.green (sender,
                String.format ("PvP is now %s for %s", PvPUtils.getEnabledString (!data.isPvPEnabled ()),
                    player.getDisplayName ()));
        }
        else
        {
            ChatUtils.yellow (sender,
                String.format ("PvP is already %s for %s", PvPUtils.getEnabledString (mode.booleanValue ()),
                    player.getDisplayName ()));
        }
    }

}
