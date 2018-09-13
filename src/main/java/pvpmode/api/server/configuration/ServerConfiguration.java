package pvpmode.api.server.configuration;

import java.util.Collection;

import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.configuration.CommonConfiguration;

public interface ServerConfiguration extends CommonConfiguration
{

    public static final String SERVER_CONFIGURATION_CATEGORY = "server";

    public static final String DEFAULT_PVP_MODE_CONFIGURATION_NAME = "Default PvP Mode";
    public static final String DEFAULT_PVP_MODE_FORCED_CONFIGURATION_NAME = "Default PvP Mode Forced";
    public static final String OVERRIDE_CHECK_INTERVAL_CONFIGURATION_NAME = "Override Check Interval (seconds)";

    public static final String PVP_TOGGLING_ENABLED_CONFIGURATION_NAME = "PvP Toggling Enabled";
    public static final String WARMUP_OFF_ON_CONFIGURATION_NAME = "Warmup off-on (seconds)";
    public static final String WARMUP_ON_OFF_CONFIGURATION_NAME = "Warmup on-off (seconds)";
    public static final String COOLDOWN_CONFIGURATION_NAME = "Cooldown (seconds)";

    public static final String INTELLIGENCE_ENABLED_CONFIGURATION_NAME = "Intelligence Enabled";
    public static final String DISTANCE_ROUNDING_FACTOR_CONFIGURATION_NAME = "Distance Rounding Factor";
    public static final String PROXIMITY_DIRECTION_SHOWN_CONFIGURATION_NAME = "Proximity Direction Shown";
    public static final String PER_PLAYER_SPYING_SETTINGS_ALLOWED_CONFIGURATION_NAME = "Per Player Spying Settings Allowed";

    public static final String ACTIVE_COMBAT_LOGGING_HANDLERS_CONFIGURATION_NAME = "Active Combat Logging Handlers";
    public static final String CSV_SEPARATOR_CONFIGURATION_NAME = "CSV Separator";

    public static final String PVP_PARTIAL_INVENTORY_LOSS_ENABLED_CONFIGURATION_NAME = "PvP Partial Inventory Loss Enabled";
    public static final String PVP_ARMOUR_ITEM_LOSS_CONFIGURATION_NAME = "PvP Armour Item Loss";
    public static final String PVP_HOTBAR_ITEM_LOSS_CONFIGURATION_NAME = "PvP Hotbar Item Loss";
    public static final String PVP_MAIN_ITEM_LOSS_CONFIGURATION_NAME = "PvP Main Item Loss";
    public static final String FAST_ITEM_TRANSFER_DISABLED_CONFIGURATION_NAME = "Fast Item Transfer Disabled";

    public static final String PVE_PARTIAL_INVENTORY_LOSS_ENABLED_CONFIGURATION_NAME = "PvE Partial Inventory Loss Enabled";
    public static final String PVE_ARMOUR_ITEM_LOSS_CONFIGURATION_NAME = "PvE Armour Item Loss";
    public static final String PVE_HOTBAR_ITEM_LOSS_CONFIGURATION_NAME = "PvE Hotbar Item Loss";
    public static final String PVE_MAIN_ITEM_LOSS_CONFIGURATION_NAME = "PvE Main Item Loss";

    public static final String ARMOUR_INVENTORY_SEARCH_EXTENDED_CONFIGURATION_NAME = "Armour Inventory Search Extended";
    public static final String HOTBAR_INVENTORY_SEARCH_EXTENDED_CONFIGURATION_NAME = "Hotbar Inventory Search Extended";
    public static final String MAIN_INVENTORY_SEARCH_EXTENDED_CONFIGURATION_NAME = "Main Inventory Search Extended";
    public static final String INDIRECT_PVP_ALLOWED_CONFIGURATION_NAME = "Indirect PvP Allowed";

    public static final String PVP_TIMER_CONFIGURATION_NAME = "PvP Timer (seconds)";
    public static final String BLOCKED_COMMANDS_CONFIGURATION_NAME = "Blocked Commands";

    public static final String GLOBAL_CHAT_MESSAGES_PREFIXED_CONFIGURATION_NAME = "Global Chat Messages Prefixed";
    public static final String GLOBAT_CHAT_MESSAGE_PREFIX_CONFIGURATION_NAME = "Global Chat Message Prefix";
    public static final String PVP_ENABLED_ANNOUNCED_GLOBALLY_CONFIGURATION_NAME = "PvP Enabled Announced Globally";
    public static final String PVP_DISABLED_ANNOUNCED_GLOBALLY_CONFIGURATION_NAME = "PvP Disabled Announced Globally";

    public static final String PVP_TOGGLING_CONFIGURATION_CATEGORY = SERVER_CONFIGURATION_CATEGORY
        + ".pvp_toggling";
    public static final String INTELLIGENCE_CONFIGURATION_CATEGORY = SERVER_CONFIGURATION_CATEGORY
        + ".intelligence";
    public static final String COMBAT_LOGGING_CONFIGURATION_CATEGORY = SERVER_CONFIGURATION_CATEGORY
        + ".combat_logging";
    public static final String CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY = COMBAT_LOGGING_CONFIGURATION_CATEGORY
        + ".csv";
    public static final String PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY = SERVER_CONFIGURATION_CATEGORY
        + ".partial_inventory_loss";
    public static final String PARTIAL_INVENTORY_LOSS_PVP_CONFIGURATION_CATEGORY = PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY
        + ".pvp";
    public static final String PARTIAL_INVENTORY_LOSS_PVE_CONFIGURATION_CATEGORY = PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY
        + ".pve";
    public static final String PVP_COMBAT_CONFIGURATION_CATEGORY = SERVER_CONFIGURATION_CATEGORY
        + ".pvp_combat";
    public static final String CHAT_MESSAGES_CONFIGURATION_CATEGORY = SERVER_CONFIGURATION_CATEGORY
        + ".chat_messages";

    public EnumPvPMode getDefaultPvPMode ();

    public boolean isDefaultPvPModeForced ();

    public int getOverrideCheckInterval ();

    public boolean isPvPTogglingEnabled ();

    public int getWarmupOffOn ();

    public int getWarmupOnOff ();

    public int getCooldown ();

    public boolean isIntelligenceEnabled ();

    public int getDistanceRoundingFactor ();

    public boolean isProximityDirectionShown ();

    public boolean arePerPlayerSpyingSettingsAllowed ();

    public Collection<String> getActiveCombatLoggingHandlers ();

    public String getCSVSeparator ();

    public boolean isPvPPartialInventoryLossEnabled ();

    public int getPvPArmourItemLoss ();

    public int getPvPHotbarItemLoss ();

    public int getPvPMainItemLoss ();

    public boolean isFastItemTransferDisabled ();

    public boolean isPvEPartialInventoryLossEnabled ();

    public int getPvEArmourItemLoss ();

    public int getPvEHotbarItemLoss ();

    public int getPvEMainItemLoss ();

    public boolean isArmourInventorySearchExtended ();

    public boolean isHotbarInventorySearchExtended ();

    public boolean isMainInventorySearchExtended ();

    public boolean isIndirectPvPAllowed ();

    public int getPvPTimer ();

    public Collection<String> getBlockedCommands ();

    public boolean areGlobalChatMessagesPrefixed ();

    public String getGlobalChatMessagePrefix ();

    public boolean isPvPEnabledAnnouncedGlobally ();

    public boolean isPvPDisabledAnnouncedGlobally ();

}
