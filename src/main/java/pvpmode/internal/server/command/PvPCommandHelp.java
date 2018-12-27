package pvpmode.internal.server.command;

import java.util.*;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.*;
import pvpmode.PvPMode;
import pvpmode.api.server.command.ServerCommandConstants;
import pvpmode.api.server.utils.*;

public class PvPCommandHelp extends AbstractPvPCommand
{
    @Override
    public String getCommandName ()
    {
        return ServerCommandConstants.PVPHELP_COMMAND_NAME;
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return ServerCommandConstants.PVPHELP_COMMAND_USAGE;
    }

    @Override
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        return Arrays.asList (Triple.of ("pvphelp ", "<command>", "Shows more help about a command."));
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();
        messages.add (Triple.of ("pvphelp", "",
            "Displays an overview about the commands of the PvP Mode Mod, with a very short description per command."));
        messages.add (Triple.of ("pvphelp ", "<command>",
            "Shows a detailed help message about the usage and purpose of the specified command. Works only for commands of the PvP Mode Mod."));
        return messages;
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "Allows you to get information about the commands of the PvP Mode Mod ingame.";
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args, String[] originalArgs)
    {
        Collection<AbstractPvPCommand> commands = PvPMode.instance.getServerProxy ().getServerCommands ();
        if (args.length == 0)
        {
            ServerChatUtils.green (sender, "------ PvP Mode Help ------");
            ServerChatUtils.blue (sender, "# General commands");
            postShortCommandHelp (sender, this,
                EnumChatFormatting.DARK_GREEN,
                EnumChatFormatting.GREEN);

            for (AbstractPvPCommand command : commands)
            {
                if (command != this && !command.isAdminCommand ())
                {
                    postShortCommandHelp (sender, command);
                }
            }

            ServerChatUtils.blue (sender, "# Admin commands");

            for (AbstractPvPCommand command : commands)
            {
                if (command != this && command.isAdminCommand ())
                {
                    postShortCommandHelp (sender, command);
                }
            }

            ServerChatUtils.green (sender, "-------------------------");
        }
        else
        {
            String command = args[0];

            // Query all server commands and look for the specified one
            boolean foundMatch = false;
            for (AbstractPvPCommand pvpCommand : commands)
            {
                if (PvPServerUtils.matches (pvpCommand, command))
                {
                    postLongCommandHelp (sender, pvpCommand);
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch)
            {
                ServerChatUtils.red (sender,
                    String.format ("The command \"%s\" doesn't exist or isn't a command of PvP Mode", command));
            }

        }
    }

    private void postShortCommandHelp (ICommandSender sender, AbstractPvPCommand command)
    {
        postCommandHelp (sender, command, command::getShortHelpMessages);
    }

    private void postShortCommandHelp (ICommandSender sender, AbstractPvPCommand command,
        EnumChatFormatting commandColor,
        EnumChatFormatting textColor)
    {
        postCommandHelp (sender, command, command::getShortHelpMessages, commandColor, textColor);
    }

    private void postLongCommandHelp (ICommandSender sender, AbstractPvPCommand command)
    {
        ServerChatUtils.green (sender, "------ PvP Mode Help ------");
        ChatComponentText usageText = new ChatComponentText ("General usage: ");
        usageText.getChatStyle ().setColor (EnumChatFormatting.BLUE);
        ChatComponentText usageMessage = new ChatComponentText (command.getCommandUsage (sender));
        usageMessage.getChatStyle ().setColor (EnumChatFormatting.DARK_GREEN);
        sender.addChatMessage (usageText.appendSibling (usageMessage));
        ServerChatUtils.white (sender, command.getGeneralHelpMessage (sender));
        ServerChatUtils.blue (sender, "Subcommand explanation:");
        postCommandHelp (sender, command, command::getLongHelpMessages);
        ServerChatUtils.green (sender, "-------------------------");
    }

    private void postCommandHelp (ICommandSender sender, AbstractPvPCommand command,
        Function<ICommandSender, Collection<Triple<String, String, String>>> messageFunction)
    {
        messageFunction.apply (sender).forEach (message ->
        {
            postCommandHelp (sender, message.getLeft (), message.getMiddle (), message.getRight ());
        });
    }

    private void postCommandHelp (ICommandSender sender, AbstractPvPCommand command,
        Function<ICommandSender, Collection<Triple<String, String, String>>> messageFunction,
        EnumChatFormatting commandColor,
        EnumChatFormatting textColor)
    {
        messageFunction.apply (sender).forEach (message ->
        {
            postCommandHelp (sender, message.getLeft (), message.getMiddle (), message.getRight (), commandColor,
                textColor);
        });
    }

    private void postCommandHelp (ICommandSender sender, String commandName, String commandUsage, String help)
    {
        this.postCommandHelp (sender, commandName, commandUsage, help, EnumChatFormatting.GRAY,
            EnumChatFormatting.WHITE);
    }

    private void postCommandHelp (ICommandSender sender, String commandName, String commandUsage, String help,
        EnumChatFormatting commandColor, EnumChatFormatting textColor)
    {
        ChatComponentText commandPart = new ChatComponentText (
            ("/" + commandName.trim () + " " + commandUsage).trim ());
        commandPart.getChatStyle ().setChatClickEvent (new ClickEvent (Action.SUGGEST_COMMAND, "/" + commandName))
            .setColor (commandColor);
        ChatComponentText helpPart = new ChatComponentText (": " + help);
        helpPart.getChatStyle ().setColor (textColor);
        sender.addChatMessage (commandPart.appendSibling (helpPart));
    }

    @Override
    public List<?> addTabCompletionOptions (ICommandSender sender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord (args, "pvp", "pvpadmin", "pvplist", "pvphelp",
                "pvpconfig");
        return null;
    }
}
