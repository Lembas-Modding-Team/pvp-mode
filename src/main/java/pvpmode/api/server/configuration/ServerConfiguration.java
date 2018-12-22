package pvpmode.api.server.configuration;

import static pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit.*;

import java.util.*;

import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.configuration.CommonConfiguration;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.log.LogHandlerConstants;
import pvpmode.api.server.utils.ServerChatUtils;

/**
 * The configuration interface for the PvP Mode Mod server configuration.
 * 
 * @author CraftedMods
 *
 */
@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "=" + ServerConfiguration.SERVER_CONFIG_PID)
public interface ServerConfiguration extends CommonConfiguration
{

    public static final String SERVER_CONFIG_PID = "pvp-mode-server";

    public static final String SERVER_CATEGORY = "server";

    public static final String PVP_TOGGLING_CATEGORY = SERVER_CATEGORY
        + ".pvp_toggling";
    public static final String INTELLIGENCE_CATEGORY = SERVER_CATEGORY
        + ".intelligence";
    public static final String COMBAT_LOGGING_CATEGORY = SERVER_CATEGORY
        + ".combat_logging";
    public static final String CSV_COMBAT_LOGGING_CATEGORY = COMBAT_LOGGING_CATEGORY
        + ".csv";
    public static final String PARTIAL_INVENTORY_LOSS_CATEGORY = SERVER_CATEGORY
        + ".partial_inventory_loss";
    public static final String PARTIAL_INVENTORY_LOSS_PVP_CATEGORY = PARTIAL_INVENTORY_LOSS_CATEGORY
        + ".pvp";
    public static final String PARTIAL_INVENTORY_LOSS_PVE_CATEGORY = PARTIAL_INVENTORY_LOSS_CATEGORY
        + ".pve";
    public static final String PVP_COMBAT_CATEGORY = SERVER_CATEGORY
        + ".pvp_combat";
    public static final String CHAT_MESSAGES_CATEGORY = SERVER_CATEGORY
        + ".chat_messages";

    @ConfigurationPropertyGetter(category = SERVER_CATEGORY)
    public default EnumPvPMode getDefaultPvPMode ()
    {
        return EnumPvPMode.OFF;
    };

    @ConfigurationPropertyGetter(category = SERVER_CATEGORY)
    public default boolean isDefaultPvPModeForced ()
    {
        return false;
    };

    @ConfigurationPropertyGetter(category = SERVER_CATEGORY, unit = SECONDS)
    @Bounded(min = "-1", max = "60")
    public default int getOverrideCheckInterval ()
    {
        return 10;
    };

    @ConfigurationPropertyGetter(category = PVP_TOGGLING_CATEGORY)
    public default boolean isPvPTogglingEnabled ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(internalName = "warmup_off-on", category = PVP_TOGGLING_CATEGORY, unit = SECONDS)
    @Bounded(min = "0")
    public default int getWarmupOffOn ()
    {
        return 30;
    }

    @ConfigurationPropertyGetter(internalName = "warmup_on-off", category = PVP_TOGGLING_CATEGORY, unit = SECONDS)
    @Bounded(min = "0")
    public default int getWarmupOnOff ()
    {
        return 300;
    }

    @ConfigurationPropertyGetter(category = PVP_TOGGLING_CATEGORY, unit = SECONDS)
    @Bounded(min = "0")
    public default int getCooldown ()
    {
        return 900;
    }

    @ConfigurationPropertyGetter(category = INTELLIGENCE_CATEGORY)
    public default boolean isIntelligenceEnabled ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = INTELLIGENCE_CATEGORY, unit = BLOCKS)
    @Bounded(min = "1")
    public default int getDistanceRoundingFactor ()
    {
        return 100;
    }

    @ConfigurationPropertyGetter(category = INTELLIGENCE_CATEGORY)
    public default boolean isProximityDirectionShown ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = INTELLIGENCE_CATEGORY)
    public default boolean arePerPlayerSpyingSettingsAllowed ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = COMBAT_LOGGING_CATEGORY)
    public default Set<String> getActiveCombatLoggingHandlers ()
    {
        return new HashSet<> (Arrays.asList (LogHandlerConstants.CSV_CONFIG_NAME));
    }

    @ConfigurationPropertyGetter(category = COMBAT_LOGGING_CATEGORY)
    public default String getCSVSeparator ()
    {
        return ";";
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_PVP_CATEGORY)
    public default boolean isPvPPartialInventoryLossEnabled ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_PVP_CATEGORY, unit = Unit.ITEM_STACKS)
    @Bounded(min = "0", max = "4")
    public default int getPvPArmourItemLoss ()
    {
        return 1;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_PVP_CATEGORY, unit = Unit.ITEM_STACKS)
    @Bounded(min = "0", max = "9")
    public default int getPvPHotbarItemLoss ()
    {
        return 2;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_PVP_CATEGORY, unit = Unit.ITEM_STACKS)
    @Bounded(min = "0", max = "27")
    public default int getPvPMainItemLoss ()
    {
        return 0;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_PVP_CATEGORY)
    public default boolean isFastItemTransferDisabled ()
    {
        return false;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_PVE_CATEGORY)
    public default boolean isPvEPartialInventoryLossEnabled ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_PVE_CATEGORY, unit = Unit.ITEM_STACKS)
    @Bounded(min = "0", max = "4")
    public default int getPvEArmourItemLoss ()
    {
        return 1;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_PVE_CATEGORY, unit = Unit.ITEM_STACKS)
    @Bounded(min = "0", max = "9")
    public default int getPvEHotbarItemLoss ()
    {
        return 2;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_PVE_CATEGORY, unit = Unit.ITEM_STACKS)
    @Bounded(min = "0", max = "4")
    public default int getPvEMainItemLoss ()
    {
        return 0;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_CATEGORY)
    public default boolean isArmourInventorySearchExtended ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_CATEGORY)
    public default boolean isHotbarInventorySearchExtended ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_CATEGORY)
    public default boolean isMainInventorySearchExtended ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = PARTIAL_INVENTORY_LOSS_CATEGORY)
    public default boolean isIndirectPvPAllowed ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = PVP_COMBAT_CATEGORY, unit = Unit.SECONDS)
    @Bounded(min = "10", max = "300")
    public default int getPvPTimer ()
    {
        return 45;
    }

    @ConfigurationPropertyGetter(category = PVP_COMBAT_CATEGORY)
    public default Set<String> getBlockedCommands ()
    {
        return new HashSet<> ();
    }

    @ConfigurationPropertyGetter(category = CHAT_MESSAGES_CATEGORY)
    public default boolean areGlobalChatMessagesPrefixed ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = CHAT_MESSAGES_CATEGORY)
    public default String getGlobalChatMessagePrefix ()
    {
        return ServerChatUtils.DEFAULT_CHAT_MESSAGE_PREFIX;
    }

    @ConfigurationPropertyGetter(category = CHAT_MESSAGES_CATEGORY)
    public default boolean isPvPEnabledAnnouncedGlobally ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = CHAT_MESSAGES_CATEGORY)
    public default boolean isPvPDisabledAnnouncedGlobally ()
    {
        return false;
    }

}
