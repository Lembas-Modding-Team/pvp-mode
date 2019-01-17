package pvpmode.internal.server.command;

import java.util.*;

import org.apache.commons.lang3.*;
import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.server.utils.PvPServerUtils;

public abstract class AbstractPvPCommand extends CommandBase
{

    /**
     * Returns a short description for this command (or every sub-command). The
     * triple contains firstly the (sub-)command name, then the command usage and
     * finally the short help message.
     */
    public abstract Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender);

    /**
     * Returns a detailed description for this command (or every sub-command). The
     * triple contains firstly the (sub-)command name, then the command usage and
     * finally the detailed help message.
     */
    public abstract Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender);

    /**
     * Returns a help message regarding the command in general.
     */
    public abstract String getGeneralHelpMessage (ICommandSender sender);

    protected void usageError (ICommandSender sender)
    {
        throw new WrongUsageException (getCommandUsage (sender));
    }

    protected void requireArgumentLength (ICommandSender sender, String[] args, int requiredLength)
    {
        if (args.length != requiredLength)
        {
            usageError (sender);
        }
    }

    protected void requireMinLength (ICommandSender sender, String[] args, int minLength)
    {
        if (args.length < minLength)
        {
            usageError (sender);
        }
    }

    protected String requireArguments (ICommandSender sender, String[] args, int index, String... requiredArguments)
    {
        requireMinLength (sender, args, index + 1);
        if (!ArrayUtils.contains (requiredArguments, args[index]))
        {
            usageError (sender);
        }
        return args[index];
    }

    protected void requireNonFlyingSender (EntityPlayer sender)
    {
        if (PvPServerUtils.canFly (sender))
            throw new CommandException ("You cannot use this command while able to fly");
    }

    protected void requireNonCreativeSender (EntityPlayer sender)
    {
        if (PvPServerUtils.isCreativeMode (sender))
            throw new CommandException ("You cannot use this command while in creative mode");
    }

    protected void requireNonOverriddenSender (EntityPlayer sender)
    {
        if (PvPServerUtils.isPvPModeOverriddenForPlayer (sender))
            throw new CommandException ("You cannot use this command while your PvP mode is overridden");
    }

    protected void requireNonPvPSender (EntityPlayer sender)
    {
        if (PvPServerUtils.isInPvP (sender))
            throw new CommandException ("You cannot use this command while in PvP combat");
    }

    protected void requireNonFlyingPlayer (EntityPlayer player)
    {
        if (PvPServerUtils.canFly (player))
            throw new CommandException ("You cannot use this command while that player is able to fly");
    }

    protected void requireNonCreativePlayer (EntityPlayer player)
    {
        if (PvPServerUtils.isCreativeMode (player))
            throw new CommandException ("You cannot use this command while that player is in creative mode");
    }

    protected void requireNonOverriddenPlayer (EntityPlayer player)
    {
        if (PvPServerUtils.isPvPModeOverriddenForPlayer (player))
            throw new CommandException ("You cannot use this command while the PvP mode of that player is overridden");
    }

    protected void requireNonPvPPlayer (EntityPlayer player)
    {
        if (PvPServerUtils.isInPvP (player))
            throw new CommandException ("You cannot use this command while that player is in PvP combat");
    }

    protected void requireSenderWithPvPEnabled (EntityPlayer sender)
    {
        if (PvPServerUtils.getPvPMode (sender) != EnumPvPMode.ON)
            throw new CommandException ("You cannot use this command while PvP is disabled for you");
    }

    protected void featureDisabled ()
    {
        throw new CommandException ("This feature is disabled on this server");
    }

    public boolean isAdminCommand ()
    {
        return false;
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        if (!isAdminCommand ())
            return true;
        return super.canCommandSenderUseCommand (sender);
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {

        List<String> parsedArgs = new ArrayList<> ();

        if (StringUtils.countMatches (StringUtils.join (args), "\"") % 2 != 0)
            throw new SyntaxErrorException (
                "The command contains an odd number of quotes, which leads to incorrect argument parsing");

        StringBuilder argBuilder = new StringBuilder ();
        boolean isBuildingArg = false;

        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];

            boolean doesArgStartWithQuote = arg.startsWith ("\"");
            boolean doesArgEndWithQuote = arg.endsWith ("\"");

            arg = arg.replaceAll ("\"", "");

            if (doesArgStartWithQuote && !isBuildingArg)
            {
                isBuildingArg = true;
                argBuilder.append (arg);
            }
            else if (isBuildingArg)
            {
                argBuilder.append (" " + arg);
            }
            else
            {
                parsedArgs.add (arg);
            }

            if (isBuildingArg && (doesArgEndWithQuote || i == args.length - 1))
            {
                isBuildingArg = false;
                String argString = argBuilder.toString ();
                if (!argString.isEmpty ())
                    parsedArgs.add (argString);
                argBuilder.delete (0, argBuilder.length ());
            }
        }
        this.processCommand (sender, parsedArgs.toArray (new String[parsedArgs.size ()]), args);
    }

    protected abstract void processCommand (ICommandSender sender, String[] parsedArgs, String[] originalArgs);

    @Override
    public List<?> addTabCompletionOptions (ICommandSender sender, String[] args)
    {
        if (canCommandSenderUseCommand (sender))
            return getTabCompletionOptions (sender, args);
        return null;
    }

    public abstract List<String> getTabCompletionOptions (ICommandSender sender, String[] args);

}
