package pvpmode.command;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import pvpmode.PvPUtils;

public abstract class AbstractPvPCommand extends CommandBase
{

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

    protected void requireArgument (ICommandSender sender, String[] args, int index, String requiredArgument)
    {
        requireMinLength (sender, args, index + 1);
        if (!args[index].equals (requiredArgument))
        {
            usageError (sender);
        }
    }

    protected void requireNonFlyingSender (EntityPlayer sender)
    {
        if (PvPUtils.canFly (sender))
            throw new CommandException ("You cannot use this command while you're able to fly");
    }

    protected void requireNonCreativeSender (EntityPlayer sender)
    {
        if (PvPUtils.isCreativeMode (sender))
            throw new CommandException ("You cannot use this command while you're in creative mode");
    }

    protected void requireNonOverriddenSender (EntityPlayer sender)
    {
        if (PvPUtils.isPvPModeOverriddenForPlayer (PvPUtils.getPvPData (sender)))
            throw new CommandException ("You cannot use this command while your PvP mode is overridden");
    }

    protected void requireNonPvPSender (EntityPlayer sender)
    {
        if (PvPUtils.isInPvP (PvPUtils.getPvPData (sender)))
            throw new CommandException ("You cannot use this command while you're in PvP");
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
        if (PvPUtils.isPvPModeOverriddenForPlayer (PvPUtils.getPvPData (player)))
            throw new CommandException ("You cannot use this command while the PvP mode of that player is overridden");
    }

    protected void requireNonPvPPlayer (EntityPlayer player)
    {
        if (PvPUtils.isInPvP (PvPUtils.getPvPData (player)))
            throw new CommandException ("You cannot use this command while that player is in PvP");
    }

}
