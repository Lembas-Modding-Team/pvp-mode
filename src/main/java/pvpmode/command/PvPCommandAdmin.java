package pvpmode.command;

import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
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
        return "/pvpadmin <player> [on|off|default] OR /pvpadmin info <player>";
    }

    @Override
    public int getRequiredPermissionLevel ()
    {
        return 2;
    }

    @Override
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        ArrayList<Triple<String, String, String>> messages = new ArrayList<> ();
        messages.add (Triple.of ("pvpadmin ", "<player> [on|off|default]", "Toggles PvP for another player."));
        messages.add (Triple.of ("pvpadmin info ", "<player>", "Displays another player's PvP stats."));
        return messages;
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        ArrayList<Triple<String, String, String>> messages = new ArrayList<> ();
        messages.add (Triple.of ("pvpadmin ", "<player> [on|off|default]",
            "Either toggle or set the PvP mode of another player to a specified mode (ON or OFF or the default one set in the configs). The player will be informed about that."));
        messages.add (Triple.of ("pvpadmin info ", "<player>",
            "Displays the PvP mode, the spying settings, the warmup, cooldown and the PvP timer, whether the PvP mode is overridden, and other PvP Mode Mod related stats about the specified player."));
        return messages;
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "For operators. Allows them, to manage and view the PvP mode (ON or OFF) and other stats of another player. That player mustn't be able to fly, not in creative mode, not in PvP combat, and the PvP mode of that player mustn't be overridden if you want to modify the PvP stats of that player.";
    }

    @Override
    public void processCommand (ICommandSender admin, String[] args)
    {

        requireMinLength (admin, args, 1);

        if (args[0].equals ("info"))
        {
            requireMinLength (admin, args, 2);
            PvPUtils.displayPvPStats (admin,
                CommandBase.getPlayer (admin, args[1]));
        }
        else
        {
            EntityPlayerMP player = CommandBase.getPlayer (admin, args[0]);

            PvPData data = PvPUtils.getPvPData (player);

            requireNonCreativePlayer (player);
            requireNonFlyingPlayer (player);
            requireNonOverriddenPlayer (player);
            requireNonPvPPlayer (player);

            if (args.length > 1)
            {
                switch (requireArguments (admin, args, 1, "on", "off", "default"))
                {
                    case "off":
                        togglePvPMode (admin, player, data, Boolean.FALSE);
                        break;
                    case "on":
                        togglePvPMode (admin, player, data, Boolean.TRUE);
                        break;
                    case "default":
                        togglePvPMode (admin, player, data, PvPMode.defaultPvPMode);
                        break;
                }
            }
            else
            {
                togglePvPMode (admin, player, data, null);
            }
        }
    }

    @Override
    public boolean isUsernameIndex (String[] args, int index)
    {
        return index == 0 ? args.length > 0 && !args[0].equals ("info") : index == 1;
    }

    @Override
    public List<?> addTabCompletionOptions (ICommandSender sender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord (args,
                ArrayUtils.add (MinecraftServer.getServer ().getAllUsernames (), "info"));
        if (args.length == 2 && args[0].equals ("info"))
            return CommandBase.getListOfStringsMatchingLastWord (args,
                MinecraftServer.getServer ().getAllUsernames ());
        if (args.length == 2 && !args[0].equals ("info"))
            return CommandBase.getListOfStringsMatchingLastWord (args,
                "on", "off", "default");
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
            data.setDefaultModeForced (data.isPvPEnabled () != PvPMode.defaultPvPMode);

            ChatUtils.green (sender,
                String.format ("PvP is now %s for %s",
                    PvPUtils.getEnabledString (!data.isPvPEnabled ()),
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
