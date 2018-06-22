package pvpmode.command;

import net.minecraft.command.*;
import pvpmode.PvPUtils;

public abstract class AbstractPvPCommand extends CommandBase
{

    protected void usageError (ICommandSender sender)
    {
        PvPUtils.red (sender, this.getCommandUsage (sender));
    }

    protected boolean requireArgumentLength (ICommandSender sender, String[] args, int requiredLength)
    {
        if (args.length != requiredLength)
        {
            usageError (sender);
            return false;
        }
        return true;
    }

    protected boolean requireMinLength (ICommandSender sender, String[] args, int minLength)
    {
        if (args.length < minLength)
        {
            usageError (sender);
            return false;
        }
        return true;
    }

    protected boolean requireArgument (ICommandSender sender, String[] args, int index, String requiredArgument)
    {
        if (!requireMinLength (sender, args, index + 1))
            return false;
        if (!args[index].equals (requiredArgument))
        {
            usageError (sender);
            return false;
        }
        return true;
    }

}
