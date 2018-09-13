package pvpmode.internal.server.configuration;

import java.util.*;

import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.*;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.utils.ServerChatUtils;
import pvpmode.internal.server.ServerProxy;

public class ServerConfigurationImpl implements ServerConfiguration
{

    private final ServerProxy server;
    private final Configuration configuration;
    private final SimpleLogger logger;

    private int warmupOffOn;
    private int cooldown;
    private int warmupOnOff;
    private boolean showProximityDirection;
    private boolean radarEnabled;
    private int distanceRoundingFactor;
    private boolean perPlayerSpyingSettingsAllowed;

    private Collection<String> activeCombatLogHandlers;
    private String csvSeparator;

    private boolean pvpPartialInventoryLossEnabled;
    private int pvpArmourItemLoss;
    private int pvpHotbarItemLoss;
    private int pvpMainItemLoss;
    private boolean fastItemTransferDisabled;
    private boolean armourInventorySearchExtended;
    private boolean hotbarInventorySearchExtended;
    private boolean mainInventorySearchExtended;
    private boolean pvePartialInventoryLossEnabled;
    private int pveArmourItemLoss;
    private int pveHotbarItemLoss;
    private int pveMainItemLoss;
    private boolean indirectPvPAllowed;

    private int overrideCheckInterval;

    private int pvpTimer;
    private Collection<String> blacklistedCommands;

    private boolean globalChatMessagesPrefixed;
    private String globalChatMessagePrefix;
    private boolean pvpEnabledAnnouncedGlobally;
    private boolean pvpDisabledAnnouncedGlobally;

    private boolean pvpTogglingEnabled;
    private EnumPvPMode defaultPvPMode;
    private boolean defaultPvPModeForced;

    private boolean soulboundItemsEnabled;
    private String soulboundTooltip;

    public static final String DEFAULT_SOULBOUND_TOOLTIP = "\u00A7r\u00A78[\u00A75Soulbound\u00A7r\u00A78]";

    public ServerConfigurationImpl (ServerProxy server, Configuration configuration)
    {
        this.server = server;
        this.configuration = configuration;
        this.logger = server.getLogger ();
    }

    @Override
    public void save ()
    {
        if (configuration.hasChanged ())
        {
            configuration.save ();
        }
    }

    @Override
    public void load ()
    {
        configuration.load ();

        warmupOffOn = configuration.getInt (WARMUP_OFF_ON_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, 30,
            0, Integer.MAX_VALUE,
            "The delay after which the PvP mode of a player with PvP OFF will be actually toggled (after initiating it).");
        cooldown = configuration.getInt (COOLDOWN_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY,
            900, 0, Integer.MAX_VALUE,
            "The duration after a PvP mode toggle while which the PvP mode cannot be toggled again.");
        warmupOnOff = configuration.getInt (WARMUP_ON_OFF_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, 300, 0,
            Integer.MAX_VALUE,
            "The delay after which the PvP mode of a player with PvP ON will be actually toggled (after initiating it).");
        showProximityDirection = configuration.getBoolean (SHOW_PROXIMITY_DIRECTION_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY,
            true,
            "Shows additionally to the proximity information the direction of the other players.");
        radarEnabled = configuration.getBoolean (RADAR_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, true,
            "If true, players with PvP mode enabled will receive proximity information about other players with PvP enabled. If allow per player spy settings is enabled this info will only be available for players who enabled spy.");
        distanceRoundingFactor = configuration.getInt (DISTANCE_ROUNDING_FACTOR_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, 100, 1,
            Integer.MAX_VALUE,
            "The factor by which the proximity information will be rounded. The distance will be displayed in a multiple of this factor.");
        perPlayerSpyingSettingsAllowed = configuration.getBoolean (PER_PLAYER_SPYING_SETTINGS_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, true,
            "If true, players can decide whether they want to enable spying or not. If enabled, they can see proximity informations of other players if they have PvP enabled, and also their proximity informations will be accessible.");

        csvSeparator = configuration.getString (CSV_SEPARATOR_CONFIGURATION_NAME,
            CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY,
            ";",
            "The separator character used between columns in the CSV file. Usually a semicolon or comma. Please note that in some countries the decimal separator is a comma. Decimal numbers will be written to the logs.")
            .trim ();
        String[] validPvPLogHandlerNames = server.getCombatLogManager ().getRegisteredHandlerNames ();

        activeCombatLogHandlers = new HashSet<> (Arrays.asList (
            configuration.getStringList (ACTIVE_COMBAT_LOG_HANDLERS_CONFIGURATION_NAME,
                SERVER_CONFIGURATION_CATEGORY, new String[]
                {server.getCombatLogManager ().getDefaultHandlerName ()},
                "Valid values: " + Arrays.toString (validPvPLogHandlerNames)
                    + ". Leave it empty (without empty lines!) to disable pvp logging.",
                validPvPLogHandlerNames)));

        pvpPartialInventoryLossEnabled = configuration.getBoolean (
            PVP_PARTIAL_INVENTORY_LOSS_ENABLED_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            true,
            "If set to true, the partial inventory loss for PvP will be enabled. If keepInventory is enabled, the player will loose a specified amount of item stacks from his hotbar and armor and main inventory slots upon death caused by PvP. The item stacks will be dropped.");
        pvpArmourItemLoss = configuration.getInt (PVP_ARMOUR_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            1, 0, 4,
            "The amount of item stacks from the armour inventory the player looses upon death caused by PvP.");
        pvpHotbarItemLoss = configuration.getInt (PVP_HOTBAR_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            2, 0, 9,
            "The amount of item stacks from the hotbar the player looses upon death caused by PvP.");
        pvpMainItemLoss = configuration.getInt (PVP_MAIN_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, 0, 0,
            27,
            "The amount of item stacks from the main inventory the player looses upon death caused by PvP.");
        fastItemTransferDisabled = configuration.getBoolean (DISABLE_FAST_ITEM_TRANSFER_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, false,
            "If true, players won't be able to transfer item stacks in their inventory with shift-clicking while in PvP");
        armourInventorySearchExtended = configuration.getBoolean (EXTEND_ARMOUR_INVENTORY_SEARCH_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If the armour inventory contains less item stacks than have to be dropped, the game will additionally look at the main inventory (first) and the hotbar for armour item stacks.");
        hotbarInventorySearchExtended = configuration.getBoolean (EXTEND_HOTBAR_INVENTORY_SEARCH_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If the hotbar contains less item stacks than have to be dropped, the game will additionally look at the main inventory for item stacks to be dropped.");
        mainInventorySearchExtended = configuration.getBoolean (EXTEND_MAIN_INVENTORY_SEARCH_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If the main inventory contains less item stacks than have to be dropped, the game will additionally look at the hotbar for item stacks to be dropped.");
        pvePartialInventoryLossEnabled = configuration.getBoolean (
            PVE_PARTIAL_INVENTORY_LOSS_ENABLED_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If set to true, the partial inventory loss for PvE (Player versus Environment) will be enabled. All deaths caused by the environment (water, height, fire, plants, ...), and also NPCs, count as PvE. If keepInventory is enabled, the player will loose a specified amount of item stacks from his hotbar and armor and main inventory slots upon death caused by PvE. The item stacks will be dropped.");
        pveArmourItemLoss = configuration.getInt (PVE_ARMOUR_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            1, 0, 4,
            "The amount of item stacks from the armour inventory the player looses upon death caused by PvE.");
        pveHotbarItemLoss = configuration.getInt (PVE_HOTBAR_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            2, 0, 9,
            "The amount of item stacks from the hotbar the player looses upon death caused by PvE.");
        pveMainItemLoss = configuration.getInt (PVE_MAIN_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, 0, 0,
            27,
            "The amount of item stacks from the main inventory the player looses upon death caused by PvE.");
        indirectPvPAllowed = configuration.getBoolean (ALLOW_INDIRECT_PVP_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If true, attacks from NPCs owned by a player (dogs, hired units, ...) will count as PvP and not as PvE.");

        overrideCheckInterval = configuration.getInt (OVERRIDE_CHECK_INTERVAL_CONFIGURATION_NAME,
            SERVER_CONFIGURATION_CATEGORY, 10, -1, 60,
            "Specifies how often the mod checks for PvP mode overrides. If set to zero, the checks will be executed every tick. Set it to -1 to disable the PvP mode overrides.");

        pvpTimer = configuration.getInt (PVP_TIMER_CONFIGURATION_NAME,
            SERVER_CONFIGURATION_CATEGORY, 45, 10, 300,
            "Specifies the time interval after a combat event while which all involved players are seen as \"in PvP\".");
        blacklistedCommands = Arrays.asList (
            configuration.getStringList (COMMAND_BLACKLIST_CONFIGURATION_NAME,
                SERVER_CONFIGURATION_CATEGORY,
                new String[] {},
                "Commands in this list cannot be executed by players who are in PvP. Note that this only applies for commands which are registered on the server. The commands are specified by the command name, without the slash. Invalid command names will be ignored."));

        globalChatMessagesPrefixed = configuration.getBoolean (
            PREFIX_GLOBAL_CHAT_MESSAGES_CONFIGURATION_NAME,
            SERVER_CONFIGURATION_CATEGORY,
            true,
            "If true, all global chat messages sent by the PvP Mode Mod will be prefixed with a configurable, global prefix.");
        globalChatMessagePrefix = configuration.getString (GLOBAT_CHAT_MESSAGE_PREFIX_CONFIGURATION_NAME,
            SERVER_CONFIGURATION_CATEGORY,
            ServerChatUtils.DEFAULT_CHAT_MESSAGE_PREFIX,
            "The prefix appended to every global chat message (if prefixing is enabled). It must not be blank. You can also use the MC formatting codes to give the prefix a color.");
        pvpEnabledAnnouncedGlobally = configuration.getBoolean (ANNOUNCE_PVP_ENABLED_GLOBALLY_CONFIGURATION_NAME,
            SERVER_CONFIGURATION_CATEGORY,
            true, "Sends a message to all players if PvP is enabled for a player.");
        pvpDisabledAnnouncedGlobally = configuration.getBoolean (ANNOUNCE_PVP_DISABLED_GLOBALLY_CONFIGURATION_NAME,
            SERVER_CONFIGURATION_CATEGORY,
            false, "Sends a message to all players if PvP is disabled for a player.");

        pvpTogglingEnabled = configuration.getBoolean (PVP_TOGGLING_ENABLED_CONFIGURATION_NAME,
            SERVER_CONFIGURATION_CATEGORY, true,
            "If true, players can decide by themselves with /pvp whether they want to have PvP enabled or not (as long as no overrides apply).");
        defaultPvPMode = EnumPvPMode.fromBoolean (configuration.getBoolean (DEFAULT_PVP_MODE_CONFIGURATION_NAME,
            SERVER_CONFIGURATION_CATEGORY, false,
            "This specifies the default PvP mode players will have (true for PvP enabled, false for disabled)."));
        defaultPvPModeForced = configuration.getBoolean (FORCE_DEFAULT_PVP_MODE_CONFIGURATION_NAME,
            SERVER_CONFIGURATION_CATEGORY, false,
            "If true, the default PvP mode will be forced for all players. For that 'Enable PvP Toggling' has to be set to false. Admins can still override the PvP mode of a player with /pvpadmin, and the override conditions still overrude this setting.");

        soulboundItemsEnabled = configuration.getBoolean (SOULBOUND_ITEMS_ENABLED_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, true,
            "If true, the soulbound command of the PvP Mode Mod will be enabled and items marked as soulbound won't be dropped. If SuffixForge is present, the soulbound command will be disabled.");
        soulboundTooltip = configuration.getString ("Soulbound Item Tooltip",
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY,
            DEFAULT_SOULBOUND_TOOLTIP,
            "The tooltip shown for soulbound items. It musn't be blank. You can use the MC formatting codes to format the tooltip.");

        configuration.addCustomCategoryComment (CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY,
            "Configuration entries related to the CSV combat logging handler");
        configuration.addCustomCategoryComment (
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            "Configuration entries related to the partial inventory loss");

        this.validateConfigurationData ();

        if (configuration.hasChanged ())
            configuration.save ();
    }

    private void validateConfigurationData ()
    {
        if (csvSeparator.length () != 1)
        {
            logger.warning ("The csv separator \"%s\" is invalid. The default one will be used.",
                csvSeparator);
            csvSeparator = ";";
        }

        Iterator<String> commandIterator = blacklistedCommands.iterator ();
        while (commandIterator.hasNext ())
        {
            String commandName = commandIterator.next ();
            if (commandName.trim ().isEmpty ())
            {
                commandIterator.remove ();
            }
        }

        if (globalChatMessagePrefix.trim ().isEmpty ())
        {
            globalChatMessagePrefix = ServerChatUtils.DEFAULT_CHAT_MESSAGE_PREFIX;
            logger.warning ("The global chat message prefix is empty. A default one will be used.");
        }

        logger.info ("%d commands are blacklisted", blacklistedCommands.size ());

        if (defaultPvPModeForced && pvpTogglingEnabled)
        {
            logger.warning (
                "The configuration property '%s' is set to true but '%s' is set to false, but required to be set to true.",
                FORCE_DEFAULT_PVP_MODE_CONFIGURATION_NAME, PVP_TOGGLING_ENABLED_CONFIGURATION_NAME);
        }

        if (activeCombatLogHandlers.size () > 0)
        {
            Iterator<String> activatedHandlersIterator = activeCombatLogHandlers.iterator ();
            while (activatedHandlersIterator.hasNext ())
            {
                String handlerName = activatedHandlersIterator.next ();
                if (!server.getCombatLogManager ().isValidHandlerName (handlerName))
                {
                    logger.warning ("The pvp combat logging handler \"%s\" is not valid.",
                        handlerName);
                    activatedHandlersIterator.remove ();
                }
            }
            if (activeCombatLogHandlers.isEmpty ())
            {
                logger.warning ("No valid pvp combat logging handlers were specified. A default one will be used");
                activeCombatLogHandlers.add (server.getCombatLogManager ().getDefaultHandlerName ());
            }
        }
        else
        {
            logger.warning ("No pvp combat logging handlers were specified. PvP combat logging will be disabled.");
        }

        if (Loader.isModLoaded ("suffixforge"))
        {
            soulboundItemsEnabled = false;
            logger.info ("SuffixForge is present - the soulbound command will be disabled");
        }

        if (soulboundTooltip.trim ().isEmpty ())
        {
            soulboundTooltip = DEFAULT_SOULBOUND_TOOLTIP;
            logger.warning ("The soulbound tooltip is empty. A default one will be used.");
        }

    }

    @Override
    public int getWarmupOffOn ()
    {
        return warmupOffOn;
    }

    @Override
    public int getCooldown ()
    {
        return cooldown;
    }

    @Override
    public int getWarmupOnOff ()
    {
        return warmupOnOff;
    }

    @Override
    public boolean isShowProximityDirection ()
    {
        return showProximityDirection;
    }

    @Override
    public boolean isRadarEnabled ()
    {
        return radarEnabled;
    }

    @Override
    public int getDistanceRoundingFactor ()
    {
        return distanceRoundingFactor;
    }

    @Override
    public boolean arePerPlayerSpyingSettingsAllowed ()
    {
        return perPlayerSpyingSettingsAllowed;
    }

    @Override
    public String getCSVSeparator ()
    {
        return csvSeparator;
    }

    @Override
    public Collection<String> getActiveCombatLogHandlers ()
    {
        return activeCombatLogHandlers;
    }

    @Override
    public boolean isPvPPartialInventoryLossEnabled ()
    {
        return pvpPartialInventoryLossEnabled;
    }

    @Override
    public int getPvPArmourItemLoss ()
    {
        return pvpArmourItemLoss;
    }

    @Override
    public int getPvPHotbarItemLoss ()
    {
        return pvpHotbarItemLoss;
    }

    @Override
    public int getPvPMainItemLoss ()
    {
        return pvpMainItemLoss;
    }

    @Override
    public boolean isFastItemTransferDisabled ()
    {
        return fastItemTransferDisabled;
    }

    @Override
    public boolean isArmourInventorySearchExtended ()
    {
        return armourInventorySearchExtended;
    }

    @Override
    public boolean isHotbarInventorySearchExtended ()
    {
        return hotbarInventorySearchExtended;
    }

    @Override
    public boolean isMainInventorySearchExtended ()
    {
        return mainInventorySearchExtended;
    }

    @Override
    public boolean isPvEPartialInventoryLossEnabled ()
    {
        return pvePartialInventoryLossEnabled;
    }

    @Override
    public int getPvEArmourItemLoss ()
    {
        return pveArmourItemLoss;
    }

    @Override
    public int getPvEHotbarItemLoss ()
    {
        return pveHotbarItemLoss;
    }

    @Override
    public int getPvEMainItemLoss ()
    {
        return pveMainItemLoss;
    }

    @Override
    public boolean isIndirectPvPAllowed ()
    {
        return indirectPvPAllowed;
    }

    @Override
    public int getOverrideCheckInterval ()
    {
        return overrideCheckInterval;
    }

    @Override
    public int getPvPTimer ()
    {
        return pvpTimer;
    }

    @Override
    public Collection<String> getBlacklistedCommands ()
    {
        return blacklistedCommands;
    }

    @Override
    public boolean areGlobalChatMessagesPrefixed ()
    {
        return globalChatMessagesPrefixed;
    }

    @Override
    public String getGlobalChatMessagePrefix ()
    {
        return globalChatMessagePrefix;
    }

    @Override
    public boolean isPvPEnabledAnnouncedGlobally ()
    {
        return pvpEnabledAnnouncedGlobally;
    }

    @Override
    public boolean isPvPDisabledAnnouncedGlobally ()
    {
        return pvpDisabledAnnouncedGlobally;
    }

    @Override
    public boolean isPvPTogglingEnabled ()
    {
        return pvpTogglingEnabled;
    }

    @Override
    public EnumPvPMode getDefaultPvPMode ()
    {
        return defaultPvPMode;
    }

    @Override
    public boolean isDefaultPvPModeForced ()
    {
        return defaultPvPModeForced;
    }

    @Override
    public boolean areSoulboundItemsEnabled ()
    {
        return soulboundItemsEnabled;
    }

    @Override
    public String getSoulboundItemTooltip ()
    {
        return soulboundTooltip;
    }

}
