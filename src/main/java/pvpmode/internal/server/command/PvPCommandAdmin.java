package pvpmode.internal.server.command;

import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.*;
import net.minecraft.entity.player.*;
import net.minecraft.server.MinecraftServer;
import pvpmode.PvPMode;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.command.ServerCommandConstants;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.utils.*;
import pvpmode.internal.server.ServerProxy;

public class PvPCommandAdmin extends AbstractPvPCommand
{

    private final ServerProxy server;
    private final ServerConfiguration config;

    public PvPCommandAdmin ()
    {
        server = PvPMode.instance.getServerProxy ();
        config = server.getConfiguration ();
    }

    @Override
    public String getCommandName ()
    {
        return ServerCommandConstants.PVPADMIN_COMMAND_NAME;
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return ServerCommandConstants.PVPADMIN_COMMAND_USAGE;
    }

    @Override
    public boolean isAdminCommand ()
    {
        return true;
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
            PvPServerUtils.displayPvPStats (admin,
                CommandBase.getPlayer (admin, args[1]));
        }
        else
        {
            EntityPlayerMP player = CommandBase.getPlayer (admin, args[0]);

            PvPData data = PvPServerUtils.getPvPData (player);

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
                        togglePvPMode (admin, player, data, config.getDefaultPvPMode ().toBoolean ());
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
            ServerChatUtils.red (player, "Your PvP mode is being toggled by an admin");
            data.setPvPWarmup (PvPServerUtils.getTime ());
            data.setDefaultModeForced (data.isPvPEnabled () != config.getDefaultPvPMode ().toBoolean ());

            ServerChatUtils.green (sender,
                String.format ("PvP is now %s for %s",
                    PvPCommonUtils.getEnabledString (!data.isPvPEnabled ()),
                    player.getDisplayName ()));
        }
        else
        {
            ServerChatUtils.yellow (sender,
                String.format ("PvP is already %s for %s", PvPCommonUtils.getEnabledString (mode.booleanValue ()),
                    player.getDisplayName ()));
        }
    }

}
