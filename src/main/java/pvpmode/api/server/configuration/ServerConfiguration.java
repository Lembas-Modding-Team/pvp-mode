package pvpmode.api.server.configuration;

import java.util.Collection;

import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.configuration.CommonConfiguration;

public interface ServerConfiguration extends CommonConfiguration
{

    public static final String SERVER_CONFIGURATION_CATEGORY = "server";

    public static final String WARMUP_OFF_ON_CONFIGURATION_NAME = "Warmup (seconds)";
    public static final String COOLDOWN_CONFIGURATION_NAME = "Cooldown (seconds)";
    public static final String WARMUP_ON_OFF_CONFIGURATION_NAME = "Warmup on-off (seconds)";
    public static final String SHOW_PROXIMITY_DIRECTION_CONFIGURATION_NAME = "Show Proximity Direction";

    public static final String RADAR_CONFIGURATION_NAME = "Radar";
    public static final String DISTANCE_ROUNDING_FACTOR_CONFIGURATION_NAME = "Distance Rounding Factor";
    public static final String PER_PLAYER_SPYING_SETTINGS_CONFIGURATION_NAME = "Allow Per Player Spying Settings";

    public static final String CSV_SEPARATOR_CONFIGURATION_NAME = "CSV separator";
    public static final String ACTIVE_COMBAT_LOG_HANDLERS_CONFIGURATION_NAME = "Active Pvp Logging Handlers";

    public static final String PVP_PARTIAL_INVENTORY_LOSS_ENABLED_CONFIGURATION_NAME = "Enable Partial Inventory Loss";
    public static final String PVP_ARMOUR_ITEM_LOSS_CONFIGURATION_NAME = "Armour Item Loss";
    public static final String PVP_HOTBAR_ITEM_LOSS_CONFIGURATION_NAME = "Hotbar Item Loss";
    public static final String PVP_MAIN_ITEM_LOSS_CONFIGURATION_NAME = "Main Item Loss";
    public static final String DISABLE_FAST_ITEM_TRANSFER_CONFIGURATION_NAME = "Disable Fast Item Transfer";
    public static final String EXTEND_ARMOUR_INVENTORY_SEARCH_CONFIGURATION_NAME = "Extend Armour Inventory Search";
    public static final String EXTEND_HOTBAR_INVENTORY_SEARCH_CONFIGURATION_NAME = "Extend Hotbar Inventory Search";
    public static final String EXTEND_MAIN_INVENTORY_SEARCH_CONFIGURATION_NAME = "Extend Main Inventory Search";
    public static final String PVE_PARTIAL_INVENTORY_LOSS_ENABLED_CONFIGURATION_NAME = "Enable Partial Inventory Loss For PvE";
    public static final String PVE_ARMOUR_ITEM_LOSS_CONFIGURATION_NAME = "PvE Armour Item Loss";
    public static final String PVE_HOTBAR_ITEM_LOSS_CONFIGURATION_NAME = "PvE Hotbar Item Loss";
    public static final String PVE_MAIN_ITEM_LOSS_CONFIGURATION_NAME = "PvE Main Item Loss";
    public static final String ALLOW_INDIRECT_PVP_CONFIGURATION_NAME = "Allow Indirect PvP";

    public static final String OVERRIDE_CHECK_INTERVAL_CONFIGURATION_NAME = "PvP Mode Override Check Interval (Seconds)";

    public static final String PVP_TIMER_CONFIGURATION_NAME = "PvP Timer (Seconds)";
    public static final String COMMAND_BLACKLIST_CONFIGURATION_NAME = "Command Blacklist";

    public static final String PREFIX_GLOBAL_CHAT_MESSAGES_CONFIGURATION_NAME = "Prefix Global Chat Messages";
    public static final String GLOBAT_CHAT_MESSAGE_PREFIX_CONFIGURATION_NAME = "Global Chat Message Prefix";
    public static final String ANNOUNCE_PVP_ENABLED_GLOBALLY_CONFIGURATION_NAME = "Announce PvP Enabled Globally";
    public static final String ANNOUNCE_PVP_DISABLED_GLOBALLY_CONFIGURATION_NAME = "Announce PvP Disabled Globally";

    public static final String PVP_TOGGLING_ENABLED_CONFIGURATION_NAME = "Enable PvP Toggling";
    public static final String DEFAULT_PVP_MODE_CONFIGURATION_NAME = "Default PvP Mode";
    public static final String FORCE_DEFAULT_PVP_MODE_CONFIGURATION_NAME = "Force Default PvP Mode";
    
    public static final String SOULBOUND_ITEMS_ENABLED_CONFIGURATION_NAME = "Enable Soulbound Items";
    public static final String SOULBOUND_ITEM_TOOLTIP_CONFIGURATION_NAME = "Soulbound Item Tooltip";

    public static final String CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY = SERVER_CONFIGURATION_CATEGORY
        + ".pvplogging.csv";
    public static final String PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY = SERVER_CONFIGURATION_CATEGORY
        + ".partial_inventory_loss";

    public int getWarmupOffOn ();

    public int getCooldown ();

    public int getWarmupOnOff ();

    public boolean isShowProximityDirection ();

    public boolean isRadarEnabled ();

    public int getDistanceRoundingFactor ();

    public boolean arePerPlayerSpyingSettingsAllowed ();

    public String getCSVSeparator ();

    public Collection<String> getActiveCombatLogHandlers ();

    public boolean isPvPPartialInventoryLossEnabled ();

    public int getPvPArmourItemLoss ();

    public int getPvPHotbarItemLoss ();

    public int getPvPMainItemLoss ();

    public boolean isFastItemTransferDisabled ();

    public boolean isArmourInventorySearchExtended ();

    public boolean isHotbarInventorySearchExtended ();

    public boolean isMainInventorySearchExtended ();

    public boolean isPvEPartialInventoryLossEnabled ();

    public int getPvEArmourItemLoss ();

    public int getPvEHotbarItemLoss ();

    public int getPvEMainItemLoss ();

    public boolean isIndirectPvPAllowed ();

    public int getOverrideCheckInterval ();

    public int getPvPTimer ();

    public Collection<String> getBlacklistedCommands ();

    public boolean areGlobalChatMessagesPrefixed ();

    public String getGlobalChatMessagePrefix ();

    public boolean isPvPEnabledAnnouncedGlobally ();

    public boolean isPvPDisabledAnnouncedGlobally ();

    public boolean isPvPTogglingEnabled ();

    public EnumPvPMode getDefaultPvPMode ();

    public boolean isDefaultPvPModeForced ();

    public boolean areSoulboundItemsEnabled ();

    public String getSoulboundItemTooltip ();

}
