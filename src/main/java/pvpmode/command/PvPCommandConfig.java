package pvpmode.command;

import java.util.*;

import org.apache.commons.lang3.tuple.Triple;

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
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        return Arrays.asList (Triple.of ("pvpconfig display", "", "Displays the server configuration."));
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        return Arrays.asList (Triple.of ("pvpconfig display", "",
            "Displays most of the server configuration data. Configuration entries of compatibility modules are currently not displayed."));
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "For operators. Allows them to manage and view the server configuration data related to the PvP Mode Mod.";
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        requireArguments (sender, args, 0, "display");

        displayConfiguration (sender);
    }

    private void displayConfiguration (ICommandSender sender)
    {
        ChatUtils.green (sender, "--- PvP Mode Configuration ---");
        postConfigEntry (sender, "Warmup off-on", Integer.toString (PvPMode.warmup) + "s");
        postConfigEntry (sender, "Warmup on-off", Integer.toString (PvPMode.warmupOff) + "s");
        postConfigEntry (sender, "Cooldown", Integer.toString (PvPMode.cooldown) + "s");
        postConfigEntry (sender, "Radar enabled", Boolean.toString (PvPMode.radar));
        postConfigEntry (sender, "Distance round factor", Integer.toString (PvPMode.roundFactor));
        postConfigEntry (sender, "CSV separator", PvPMode.csvSeparator);
        postConfigEntry (sender, "Combat logging handlers", PvPMode.activatedPvPLoggingHandlers.toString ());
        postConfigEntry (sender, "PvP Partial inventory loss enabled",
            Boolean.toString (PvPMode.partialInventoryLossEnabled));
        postConfigEntry (sender, "PvE Partial inventory loss enabled",
            Boolean.toString (PvPMode.enablePartialInventoryLossPvE));
        postConfigEntry (sender, "PvP Armor item loss", Integer.toString (PvPMode.inventoryLossArmour) + " items");
        postConfigEntry (sender, "PvP Hotbar item loss", Integer.toString (PvPMode.inventoryLossHotbar) + " items");
        postConfigEntry (sender, "PvP Main item loss", Integer.toString (PvPMode.inventoryLossMain) + " items");
        postConfigEntry (sender, "PvE Armor item loss", Integer.toString (PvPMode.inventoryLossArmourPvE) + " items");
        postConfigEntry (sender, "PvE Hotbar item loss", Integer.toString (PvPMode.inventoryLossHotbarPvE) + " items");
        postConfigEntry (sender, "PvE Main item loss", Integer.toString (PvPMode.inventoryLossMainPvE) + " items");
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
        postConfigEntry (sender, "Show proximity direction", Boolean.toString (PvPMode.showProximityDirection));
        postConfigEntry (sender, "Allow indirect PvP", Boolean.toString (PvPMode.allowIndirectPvP));
        postConfigEntry (sender, "Prefix global chat messages", Boolean.toString (PvPMode.prefixGlobalMessages));
        postConfigEntry (sender, "Global chat message prefix", PvPMode.globalMessagePrefix);
        postConfigEntry (sender, "PvP toggling enabled", Boolean.toString (PvPMode.pvpTogglingEnabled));
        ChatUtils.green (sender, "---------------------------");
    }

    private void postConfigEntry (ICommandSender sender, String name, String value)
    {
        ChatComponentText keyText = new ChatComponentText (name + ": ");
        keyText.getChatStyle ().setColor (EnumChatFormatting.WHITE);
        ChatComponentText valueText = new ChatComponentText (value);
        valueText.getChatStyle ().setColor (EnumChatFormatting.GRAY);
        sender.addChatMessage (keyText.appendSibling (valueText));
    }

    @Override
    public List<?> addTabCompletionOptions (ICommandSender sender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord (args, "display");
        return null;
    }
}
