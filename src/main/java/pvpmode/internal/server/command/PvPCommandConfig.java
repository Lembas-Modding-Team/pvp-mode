package pvpmode.internal.server.command;

import java.util.*;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.*;
import pvpmode.PvPMode;
import pvpmode.api.server.command.ServerCommandConstants;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.utils.ServerChatUtils;
import pvpmode.internal.server.ServerProxy;

public class PvPCommandConfig extends AbstractPvPCommand
{

    private final ServerProxy server;
    private final ServerConfiguration config;

    public PvPCommandConfig ()
    {
        server = PvPMode.instance.getServerProxy ();
        config = server.getConfiguration ();
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
        postConfigEntry (sender, "Warmup off-on", Integer.toString (config.getWarmupOffOn ()) + "s");
        postConfigEntry (sender, "Warmup on-off", Integer.toString (config.getWarmupOnOff ()) + "s");
        postConfigEntry (sender, "Cooldown", Integer.toString (config.getCooldown ()) + "s");
        postConfigEntry (sender, "Radar enabled", Boolean.toString (config.isRadarEnabled ()));
        postConfigEntry (sender, "Distance round factor", Integer.toString (config.getDistanceRoundingFactor ()));
        postConfigEntry (sender, "CSV separator", config.getCSVSeparator ());
        postConfigEntry (sender, "Combat logging handlers", config.getActiveCombatLogHandlers ().toString ());
        postConfigEntry (sender, "PvP Partial inventory loss enabled",
            Boolean.toString (config.isPvPPartialInventoryLossEnabled ()));
        postConfigEntry (sender, "PvE Partial inventory loss enabled",
            Boolean.toString (config.isPvEPartialInventoryLossEnabled ()));
        postConfigEntry (sender, "PvP Armor item loss", Integer.toString (config.getPvPArmourItemLoss ()) + " items");
        postConfigEntry (sender, "PvP Hotbar item loss",
            Integer.toString (config.getPvPHotbarItemLoss ()) + " items");
        postConfigEntry (sender, "PvP Main item loss", Integer.toString (config.getPvPMainItemLoss ()) + " items");
        postConfigEntry (sender, "PvE Armor item loss",
            Integer.toString (config.getPvEArmourItemLoss ()) + " items");
        postConfigEntry (sender, "PvE Hotbar item loss",
            Integer.toString (config.getPvEHotbarItemLoss ()) + " items");
        postConfigEntry (sender, "PvE Main item loss", Integer.toString (config.getPvEMainItemLoss ()) + " items");
        postConfigEntry (sender, "Override check interval",
            Integer.toString (config.getOverrideCheckInterval ()) + "s");
        postConfigEntry (sender, "PvP timer", Integer.toString (config.getPvPTimer ()) + "s");
        postConfigEntry (sender, "Command blacklist", config.getBlacklistedCommands ().toString ());
        postConfigEntry (sender, "Fast item transfer disabled",
            Boolean.toString (config.isFastItemTransferDisabled ()));
        postConfigEntry (sender, "Extend armour inventory search",
            Boolean.toString (config.isArmourInventorySearchExtended ()));
        postConfigEntry (sender, "Extend hotbar inventory search",
            Boolean.toString (config.isHotbarInventorySearchExtended ()));
        postConfigEntry (sender, "Extend main inventory search",
            Boolean.toString (config.isMainInventorySearchExtended ()));
        postConfigEntry (sender, "Per player spying settings",
            Boolean.toString (config.arePerPlayerSpyingSettingsAllowed ()));
        postConfigEntry (sender, "Show proximity direction", Boolean.toString (config.isShowProximityDirection ()));
        postConfigEntry (sender, "Allow indirect PvP", Boolean.toString (config.isIndirectPvPAllowed ()));
        postConfigEntry (sender, "Prefix global chat messages",
            Boolean.toString (config.areGlobalChatMessagesPrefixed ()));
        postConfigEntry (sender, "Global chat message prefix", config.getGlobalChatMessagePrefix ());
        postConfigEntry (sender, "PvP toggling enabled", Boolean.toString (config.isPvPTogglingEnabled ()));
        postConfigEntry (sender, "Default PvP mode", config.getDefaultPvPMode ().name ());
        postConfigEntry (sender, "Force default PvP mode", Boolean.toString (config.isDefaultPvPModeForced ()));
        postConfigEntry (sender, "Announce PvP enabled globally",
            Boolean.toString (config.isPvPEnabledAnnouncedGlobally ()));
        postConfigEntry (sender, "Announce PvP disabled globally",
            Boolean.toString (config.isPvPDisabledAnnouncedGlobally ()));
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
