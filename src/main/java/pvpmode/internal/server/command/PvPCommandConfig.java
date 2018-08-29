package pvpmode.internal.server.command;

import java.util.*;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.*;
import pvpmode.PvPMode;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.server.command.ServerCommandConstants;
import pvpmode.api.server.utils.ServerChatUtils;
import pvpmode.internal.server.ServerProxy;

public class PvPCommandConfig extends AbstractPvPCommand
{

    private final ServerProxy server;

    public PvPCommandConfig ()
    {
        server = PvPMode.instance.getServerProxy ();
    }

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
        postConfigEntry (sender, "Warmup off-on", Integer.toString (server.getWarmup ()) + "s");
        postConfigEntry (sender, "Warmup on-off", Integer.toString (server.getWarmupOff ()) + "s");
        postConfigEntry (sender, "Cooldown", Integer.toString (server.getCooldown ()) + "s");
        postConfigEntry (sender, "Radar enabled", Boolean.toString (server.isRadar ()));
        postConfigEntry (sender, "Distance round factor", Integer.toString (server.getRoundFactor ()));
        postConfigEntry (sender, "CSV separator", server.getCsvSeparator ());
        postConfigEntry (sender, "Combat logging handlers", server.getActivatedPvPLoggingHandlers ().toString ());
        postConfigEntry (sender, "PvP Partial inventory loss enabled",
            Boolean.toString (server.isPartialInventoryLossEnabled ()));
        postConfigEntry (sender, "PvE Partial inventory loss enabled",
            Boolean.toString (server.isEnablePartialInventoryLossPvE ()));
        postConfigEntry (sender, "PvP Armor item loss", Integer.toString (server.getInventoryLossArmour ()) + " items");
        postConfigEntry (sender, "PvP Hotbar item loss",
            Integer.toString (server.getInventoryLossHotbar ()) + " items");
        postConfigEntry (sender, "PvP Main item loss", Integer.toString (server.getInventoryLossMain ()) + " items");
        postConfigEntry (sender, "PvE Armor item loss",
            Integer.toString (server.getInventoryLossArmourPvE ()) + " items");
        postConfigEntry (sender, "PvE Hotbar item loss",
            Integer.toString (server.getInventoryLossHotbarPvE ()) + " items");
        postConfigEntry (sender, "PvE Main item loss", Integer.toString (server.getInventoryLossMainPvE ()) + " items");
        postConfigEntry (sender, "Override check interval",
            Integer.toString (server.getOverrideCheckInterval ()) + "s");
        postConfigEntry (sender, "PvP timer", Integer.toString (server.getPvpTimer ()) + "s");
        postConfigEntry (sender, "Command blacklist", server.getCommandBlacklist ().toString ());
        postConfigEntry (sender, "Fast item transfer disabled", Boolean.toString (server.isBlockShiftClicking ()));
        postConfigEntry (sender, "Extend armour inventory search",
            Boolean.toString (server.isExtendArmourInventorySearch ()));
        postConfigEntry (sender, "Extend hotbar inventory search",
            Boolean.toString (server.isExtendHotbarInventorySearch ()));
        postConfigEntry (sender, "Extend main inventory search",
            Boolean.toString (server.isExtendMainInventorySearch ()));
        postConfigEntry (sender, "Per player spying settings", Boolean.toString (server.isAllowPerPlayerSpying ()));
        postConfigEntry (sender, "Show proximity direction", Boolean.toString (server.isShowProximityDirection ()));
        postConfigEntry (sender, "Allow indirect PvP", Boolean.toString (server.isAllowIndirectPvP ()));
        postConfigEntry (sender, "Prefix global chat messages", Boolean.toString (server.isPrefixGlobalMessages ()));
        postConfigEntry (sender, "Global chat message prefix", server.getGlobalMessagePrefix ());
        postConfigEntry (sender, "PvP toggling enabled", Boolean.toString (server.isPvpTogglingEnabled ()));
        postConfigEntry (sender, "Default PvP mode", EnumPvPMode.fromBoolean (server.getDefaultPvPMode ()).name ());
        postConfigEntry (sender, "Force default PvP mode", Boolean.toString (server.isForceDefaultPvPMode ()));
        postConfigEntry (sender, "Announce PvP enabled globally",
            Boolean.toString (server.isAnnouncePvPEnabledGlobally ()));
        postConfigEntry (sender, "Announce PvP disabled globally",
            Boolean.toString (server.isAnnouncePvPDisabledGlobally ()));
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
