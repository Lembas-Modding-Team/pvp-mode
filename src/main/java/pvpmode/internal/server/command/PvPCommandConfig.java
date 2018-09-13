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
        postConfigEntry (sender, "Intelligence Enabled", Boolean.toString (config.isIntelligenceEnabled ()));
        postConfigEntry (sender, "Distance Rounding Factor", Integer.toString (config.getDistanceRoundingFactor ()));
        postConfigEntry (sender, "CSV Separator", config.getCSVSeparator ());
        postConfigEntry (sender, "Active Combat Logging Handlers", config.getActiveCombatLoggingHandlers ().toString ());
        postConfigEntry (sender, "PvP Partial Inventory Loss Enabled",
            Boolean.toString (config.isPvPPartialInventoryLossEnabled ()));
        postConfigEntry (sender, "PvE Partial Inventory Loss Enabled",
            Boolean.toString (config.isPvEPartialInventoryLossEnabled ()));
        postConfigEntry (sender, "PvP Armor Item Loss", Integer.toString (config.getPvPArmourItemLoss ()) + " stacks");
        postConfigEntry (sender, "PvP Hotbar Item Loss",
            Integer.toString (config.getPvPHotbarItemLoss ()) + " stacks");
        postConfigEntry (sender, "PvP Main Item Loss", Integer.toString (config.getPvPMainItemLoss ()) + " stacks");
        postConfigEntry (sender, "PvE Armor Item Loss",
            Integer.toString (config.getPvEArmourItemLoss ()) + " stacks");
        postConfigEntry (sender, "PvE Hotbar Item Loss",
            Integer.toString (config.getPvEHotbarItemLoss ()) + " stacks");
        postConfigEntry (sender, "PvE Main Item Loss", Integer.toString (config.getPvEMainItemLoss ()) + " stacks");
        postConfigEntry (sender, "Override Check Interval",
            Integer.toString (config.getOverrideCheckInterval ()) + "s");
        postConfigEntry (sender, "PvP Timer", Integer.toString (config.getPvPTimer ()) + "s");
        postConfigEntry (sender, "Blocked Commands", config.getBlockedCommands ().toString ());
        postConfigEntry (sender, "Fast Item Transfer Disabled",
            Boolean.toString (config.isFastItemTransferDisabled ()));
        postConfigEntry (sender, "Armour Inventory Search Extended",
            Boolean.toString (config.isArmourInventorySearchExtended ()));
        postConfigEntry (sender, "Hotbar Inventory Search Extended",
            Boolean.toString (config.isHotbarInventorySearchExtended ()));
        postConfigEntry (sender, "Main Inventory Search Extended",
            Boolean.toString (config.isMainInventorySearchExtended ()));
        postConfigEntry (sender, "Per Player Spying Settings Allowed",
            Boolean.toString (config.arePerPlayerSpyingSettingsAllowed ()));
        postConfigEntry (sender, "Proximity Direction Shown", Boolean.toString (config.isProximityDirectionShown ()));
        postConfigEntry (sender, "Indirect PvP Allowed", Boolean.toString (config.isIndirectPvPAllowed ()));
        postConfigEntry (sender, "Global Chat Messages Prefixed",
            Boolean.toString (config.areGlobalChatMessagesPrefixed ()));
        postConfigEntry (sender, "Global Chat Message Prefix", config.getGlobalChatMessagePrefix ());
        postConfigEntry (sender, "PvP Toggling Enabled", Boolean.toString (config.isPvPTogglingEnabled ()));
        postConfigEntry (sender, "Default PvP Mode", config.getDefaultPvPMode ().name ());
        postConfigEntry (sender, "Default PvP Mode Forced", Boolean.toString (config.isDefaultPvPModeForced ()));
        postConfigEntry (sender, "PvP Enabled Announced Globally",
            Boolean.toString (config.isPvPEnabledAnnouncedGlobally ()));
        postConfigEntry (sender, "PvP Disabled Announced Globally",
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
