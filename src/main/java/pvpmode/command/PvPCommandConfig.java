package pvpmode.command;

import net.minecraft.command.*;
import net.minecraft.util.EnumChatFormatting;
import pvpmode.*;

public class PvPCommandConfig extends CommandBase
{

    @Override
    public String getCommandName ()
    {
        return "pvpconfig";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return "/pvpconfig display";
    }

    @Override
    public int getRequiredPermissionLevel ()
    {
        return 2;
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        if (args.length > 0)
        {
            if (args[0].equals ("display"))
            {
                displayConfiguration (sender);
            }

            else
            {
                PvPUtils.red (sender, getCommandUsage (sender));
            }
        }
        else
        {
            PvPUtils.red (sender, getCommandUsage (sender));
        }

    }

    private void displayConfiguration (ICommandSender sender)
    {
        PvPUtils.postChatLines (sender, EnumChatFormatting.GREEN, "--- PvP Mode Configuration ---");
        postConfigEntry (sender, "Warmup", Integer.toString (PvPMode.warmup) + "s");
        postConfigEntry (sender, "Cooldown", Integer.toString (PvPMode.cooldown) + "s");
        postConfigEntry (sender, "Radar enabled", Boolean.toString (PvPMode.radar));
        postConfigEntry (sender, "Distance round factor", Integer.toString (PvPMode.roundFactor));
        postConfigEntry (sender, "CSV separator", PvPMode.csvSeparator);
        postConfigEntry (sender, "Combat logging handlers", PvPMode.activatedPvPLoggingHandlers.toString ());
        PvPUtils.postChatLines (sender, EnumChatFormatting.GREEN, "---------------------------");
    }

    private void postConfigEntry (ICommandSender sender, String name, String value)
    {
        PvPUtils.postChatLines (sender, EnumChatFormatting.WHITE, name + ": " + EnumChatFormatting.GRAY + value);
    }
}
