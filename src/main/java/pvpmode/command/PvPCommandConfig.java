package pvpmode.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.*;
import pvpmode.*;

public class PvPCommandConfig extends AbstractPvPCommand
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
        requireArguments (sender, args, 0, "display");

        displayConfiguration (sender);
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
        postConfigEntry (sender, "Partial inventory loss enabled",
            Boolean.toString (PvPMode.partialInventoryLossEnabled));
        postConfigEntry (sender, "Armor item loss", Integer.toString (PvPMode.inventoryLossArmour) + " items");
        postConfigEntry (sender, "Hotbar item loss", Integer.toString (PvPMode.inventoryLossHotbar) + " items");
        postConfigEntry (sender, "Main item loss", Integer.toString (PvPMode.inventoryLossMain) + " items");
        postConfigEntry (sender, "Override check interval", Integer.toString (PvPMode.overrideCheckInterval) + "s");
        postConfigEntry (sender, "PvP timer", Integer.toString (PvPMode.pvpTimer) + "s");
        postConfigEntry (sender, "Command blacklist", PvPMode.commandBlacklist.toString ());
        postConfigEntry (sender, "Fast item transfer disabled", Boolean.toString (PvPMode.blockShiftClicking));
        postConfigEntry (sender, "Extend armour inventory search",
            Boolean.toString (PvPMode.extendArmourInventorySearch));
        postConfigEntry (sender, "Extend hotbar inventory search",
            Boolean.toString (PvPMode.extendHotbarInventorySearch));
        postConfigEntry (sender, "Extend main inventory search", Boolean.toString (PvPMode.extendMainInventorySearch));
        postConfigEntry (sender, "Per player spying settings", Boolean.toString (PvPMode.allowPerPlayerSpying));
        PvPUtils.postChatLines (sender, EnumChatFormatting.GREEN, "---------------------------");
    }

    private void postConfigEntry (ICommandSender sender, String name, String value)
    {
        ChatComponentText keyText = new ChatComponentText (name + ": ");
        keyText.getChatStyle ().setColor (EnumChatFormatting.WHITE);
        ChatComponentText valueText = new ChatComponentText (value);
        valueText.getChatStyle ().setColor (EnumChatFormatting.GRAY);
        sender.addChatMessage (keyText.appendSibling (valueText));
    }
}
