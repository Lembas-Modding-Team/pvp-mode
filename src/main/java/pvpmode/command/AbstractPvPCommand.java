package pvpmode.command;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import pvpmode.*;

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
        throw new WrongUsageException (this.getCommandUsage (sender));
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
        if (PvPUtils.canFly (sender))
            throw new CommandException ("You cannot use this command while able to fly");
    }

    protected void requireNonCreativeSender (EntityPlayer sender)
    {
        if (PvPUtils.isCreativeMode (sender))
            throw new CommandException ("You cannot use this command while in creative mode");
    }

    protected void requireNonOverriddenSender (EntityPlayer sender)
    {
        if (PvPUtils.isPvPModeOverriddenForPlayer (sender))
            throw new CommandException ("You cannot use this command while your PvP mode is overridden");
    }

    protected void requireNonPvPSender (EntityPlayer sender)
    {
        if (PvPUtils.isInPvP (sender))
            throw new CommandException ("You cannot use this command while in PvP");
    }

    protected void requireNonFlyingPlayer (EntityPlayer player)
    {
        if (PvPUtils.canFly (player))
            throw new CommandException ("You cannot use this command while that player is able to fly");
    }

    protected void requireNonCreativePlayer (EntityPlayer player)
    {
        if (PvPUtils.isCreativeMode (player))
            throw new CommandException ("You cannot use this command while that player is in creative mode");
    }

    protected void requireNonOverriddenPlayer (EntityPlayer player)
    {
        if (PvPUtils.isPvPModeOverriddenForPlayer (player))
            throw new CommandException ("You cannot use this command while the PvP mode of that player is overridden");
    }

    protected void requireNonPvPPlayer (EntityPlayer player)
    {
        if (PvPUtils.isInPvP (player))
            throw new CommandException ("You cannot use this command while that player is in PvP");
    }

    protected void requireSenderWithPvPEnabled (EntityPlayer sender)
    {
        if (PvPUtils.getPvPMode (sender) != EnumPvPMode.ON)
            throw new CommandException ("You cannot use this command while PvP is disabled for you");
    }

}
