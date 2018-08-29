package pvpmode.internal.server.command;

import java.util.*;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.*;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.server.command.ServerCommandConstants;
import pvpmode.api.server.utils.ServerChatUtils;
import pvpmode.internal.server.ServerProxy;

public class PvPCommandConfig extends AbstractPvPCommand
{

    @Override
    public String getCommandName ()
    {
        return ServerCommandConstants.PVPCONFIG_COMMAND_NAME;
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return ServerCommandConstants.PVPCONFIG_COMMAND_USAGE;
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
        ServerChatUtils.green (sender, "--- PvP Mode Configuration ---");
        postConfigEntry (sender, "Warmup off-on", Integer.toString (ServerProxy.warmup) + "s");
        postConfigEntry (sender, "Warmup on-off", Integer.toString (ServerProxy.warmupOff) + "s");
        postConfigEntry (sender, "Cooldown", Integer.toString (ServerProxy.cooldown) + "s");
        postConfigEntry (sender, "Radar enabled", Boolean.toString (ServerProxy.radar));
        postConfigEntry (sender, "Distance round factor", Integer.toString (ServerProxy.roundFactor));
        postConfigEntry (sender, "CSV separator", ServerProxy.csvSeparator);
        postConfigEntry (sender, "Combat logging handlers", ServerProxy.activatedPvPLoggingHandlers.toString ());
        postConfigEntry (sender, "PvP Partial inventory loss enabled",
            Boolean.toString (ServerProxy.partialInventoryLossEnabled));
        postConfigEntry (sender, "PvE Partial inventory loss enabled",
            Boolean.toString (ServerProxy.enablePartialInventoryLossPvE));
        postConfigEntry (sender, "PvP Armor item loss", Integer.toString (ServerProxy.inventoryLossArmour) + " items");
        postConfigEntry (sender, "PvP Hotbar item loss", Integer.toString (ServerProxy.inventoryLossHotbar) + " items");
        postConfigEntry (sender, "PvP Main item loss", Integer.toString (ServerProxy.inventoryLossMain) + " items");
        postConfigEntry (sender, "PvE Armor item loss",
            Integer.toString (ServerProxy.inventoryLossArmourPvE) + " items");
        postConfigEntry (sender, "PvE Hotbar item loss",
            Integer.toString (ServerProxy.inventoryLossHotbarPvE) + " items");
        postConfigEntry (sender, "PvE Main item loss", Integer.toString (ServerProxy.inventoryLossMainPvE) + " items");
        postConfigEntry (sender, "Override check interval", Integer.toString (ServerProxy.overrideCheckInterval) + "s");
        postConfigEntry (sender, "PvP timer", Integer.toString (ServerProxy.pvpTimer) + "s");
        postConfigEntry (sender, "Command blacklist", ServerProxy.commandBlacklist.toString ());
        postConfigEntry (sender, "Fast item transfer disabled", Boolean.toString (ServerProxy.blockShiftClicking));
        postConfigEntry (sender, "Extend armour inventory search",
            Boolean.toString (ServerProxy.extendArmourInventorySearch));
        postConfigEntry (sender, "Extend hotbar inventory search",
            Boolean.toString (ServerProxy.extendHotbarInventorySearch));
        postConfigEntry (sender, "Extend main inventory search",
            Boolean.toString (ServerProxy.extendMainInventorySearch));
        postConfigEntry (sender, "Per player spying settings", Boolean.toString (ServerProxy.allowPerPlayerSpying));
        postConfigEntry (sender, "Show proximity direction", Boolean.toString (ServerProxy.showProximityDirection));
        postConfigEntry (sender, "Allow indirect PvP", Boolean.toString (ServerProxy.allowIndirectPvP));
        postConfigEntry (sender, "Prefix global chat messages", Boolean.toString (ServerProxy.prefixGlobalMessages));
        postConfigEntry (sender, "Global chat message prefix", ServerProxy.globalMessagePrefix);
        postConfigEntry (sender, "PvP toggling enabled", Boolean.toString (ServerProxy.pvpTogglingEnabled));
        postConfigEntry (sender, "Default PvP mode", EnumPvPMode.fromBoolean (ServerProxy.defaultPvPMode).name ());
        postConfigEntry (sender, "Force default PvP mode", Boolean.toString (ServerProxy.forceDefaultPvPMode));
        postConfigEntry (sender, "Announce PvP enabled globally",
            Boolean.toString (ServerProxy.announcePvPEnabledGlobally));
        postConfigEntry (sender, "Announce PvP disabled globally",
            Boolean.toString (ServerProxy.announcePvPDisabledGlobally));
        ServerChatUtils.green (sender, "---------------------------");
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
