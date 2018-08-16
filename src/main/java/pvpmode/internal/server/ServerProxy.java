package pvpmode.internal.server;

import static pvpmode.api.server.configuration.ServerConfigurationConstants.*;

import java.nio.file.*;
import java.util.*;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import pvpmode.api.common.configuration.CommonConfigurationConstants;
import pvpmode.api.server.log.LogHandlerConstants;
import pvpmode.api.server.utils.*;
import pvpmode.internal.common.CommonProxy;
import pvpmode.internal.server.command.*;
import pvpmode.internal.server.compatibility.modules.lotr.LOTRModCompatibilityModuleLoader;
import pvpmode.internal.server.compatibility.modules.siegeMode.SiegeModeCompatiblityModuleLoader;
import pvpmode.internal.server.compatibility.modules.suffixForge.SuffixForgeCompatibilityModuleLoader;
import pvpmode.internal.server.log.*;
import pvpmode.internal.server.overrides.OverrideManagerImpl;
import pvpmode.internal.server.utils.*;

public class ServerProxy extends CommonProxy
{

    static
    {
        ServerChatUtils.setProvider (new ServerChatUtilsProvider ());
        PvPServerUtils.setProvider (new PvPServerUtilsProvider ());
    }

    public static CombatLogManagerImpl combatLogManager;
    public static OverrideManagerImpl overrideManager;
    public static ServerConfigurationManager cfg;

    private Path combatLogDir;

    public static PvPCommand pvpCommandInstance;
    public static PvPCommandAdmin pvpadminCommandInstance;
    public static PvPCommandConfig pvpconfigCommandInstance;
    public static PvPCommandHelp pvphelpCommandInstance;
    public static PvPCommandList pvplistCommandInstance;

    public static int roundFactor;
    public static int warmup;
    public static int cooldown;
    public static boolean radar;
    public static Collection<String> activatedPvPLoggingHandlers;
    public static String csvSeparator;
    public static boolean partialInventoryLossEnabled;
    public static int inventoryLossArmour;
    public static int inventoryLossHotbar;
    public static int overrideCheckInterval;
    public static int pvpTimer;
    public static Collection<String> commandBlacklist;
    public static int inventoryLossMain;
    public static boolean blockShiftClicking;
    public static boolean extendArmourInventorySearch;
    public static boolean extendHotbarInventorySearch;
    public static boolean extendMainInventorySearch;
    public static boolean allowPerPlayerSpying;
    public static int warmupOff;
    public static boolean showProximityDirection;
    public static boolean enablePartialInventoryLossPvE;
    public static int inventoryLossArmourPvE;
    public static int inventoryLossHotbarPvE;
    public static int inventoryLossMainPvE;
    public static boolean allowIndirectPvP;
    public static boolean prefixGlobalMessages;
    public static String globalMessagePrefix;
    public static boolean pvpTogglingEnabled;
    public static boolean defaultPvPMode;
    public static boolean forceDefaultPvPMode;
    public static boolean announcePvPEnabledGlobally;
    public static boolean announcePvPDisabledGlobally;

    @Override
    public void onPreInit (FMLPreInitializationEvent event) throws Exception
    {
        super.onPreInit (event);

        combatLogDir = Paths.get (event.getModConfigurationDirectory ().getParent ().toString (), "logs", "combat");

        Files.createDirectories (combatLogDir);

        combatLogManager = new CombatLogManagerImpl (LogHandlerConstants.CSV_CONFIG_NAME);

        combatLogManager.registerCombatLogHandler (LogHandlerConstants.SIMPLE_CONFIG_NAME,
            new SimpleCombatLogHandler ());
        combatLogManager.registerCombatLogHandler (LogHandlerConstants.CSV_CONFIG_NAME, new CSVCombatLogHandler ());

        roundFactor = configuration.getInt (DISTANCE_ROUNDING_FACTOR_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, 100, 1,
            Integer.MAX_VALUE,
            "The factor by which the proximity information will be rounded. The distance will be displayed in a multiple of this factor.");
        warmup = configuration.getInt (WARMUP_OFF_ON_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, 30,
            0, Integer.MAX_VALUE,
            "The delay after which the PvP mode of a player with PvP OFF will be actually toggled (after initiating it).");
        cooldown = configuration.getInt (COOLDOWN_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY,
            900, 0, Integer.MAX_VALUE,
            "The duration after a PvP mode toggle while which the PvP mode cannot be toggled again.");
        radar = configuration.getBoolean (RADAR_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, true,
            "If true, players with PvP mode enabled will receive proximity information about other players with PvP enabled. If allow per player spy settings is enabled this info will only be available for players who enabled spy.");
        csvSeparator = configuration.getString (CSV_SEPARATOR_CONFIGURATION_NAME,
            CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY,
            ";",
            "The separator character used between columns in the CSV file. Usually a semicolon or comma. Please note that in some countries the decimal separator is a comma. Decimal numbers will be written to the logs.")
            .trim ();
        partialInventoryLossEnabled = configuration.getBoolean (PVP_PARTIAL_INVENTORY_LOSS_ENABLED_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            true,
            "If set to true, the partial inventory loss for PvP will be enabled. If keepInventory is enabled, the player will loose a specified amount of item stacks from his hotbar and armor and main inventory slots upon death caused by PvP. The item stacks will be dropped.");
        inventoryLossArmour = configuration.getInt (PVP_ARMOUR_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            1, 0, 4,
            "The amount of item stacks from the armour inventory the player looses upon death caused by PvP.");
        inventoryLossHotbar = configuration.getInt (PVP_HOTBAR_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            2, 0, 9,
            "The amount of item stacks from the hotbar the player looses upon death caused by PvP.");
        overrideCheckInterval = configuration.getInt (OVERRIDE_CHECK_INTERVAL_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, 10, -1, 60,
            "Specifies how often the mod checks for PvP mode overrides. If set to zero, the checks will be executed every tick. Set it to -1 to disable the PvP mode overrides.");
        pvpTimer = configuration.getInt (PVP_TIMER_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, 45, 10, 300,
            "Specifies the time interval after a combat event while which all involved players are seen as \"in PvP\".");
        commandBlacklist = new HashSet<> (Arrays.asList (
            configuration.getStringList (COMMAND_BLACKLIST_CONFIGURATION_NAME,
                CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY,
                new String[] {},
                "Commands in this list cannot be executed by players who are in PvP. Note that this only applies for commands which are registered on the server. The commands are specified by the command name, without the slash. Invalid command names will be ignored.")));
        inventoryLossMain = configuration.getInt (PVP_MAIN_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, 0, 0,
            27,
            "The amount of item stacks from the main inventory the player looses upon death caused by PvP.");
        blockShiftClicking = configuration.getBoolean (DISABLE_FAST_ITEM_TRANSFER_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, false,
            "If true, players won't be able to transfer item stacks in their inventory with shift-clicking while in PvP");
        extendArmourInventorySearch = configuration.getBoolean (EXTEND_ARMOUR_INVENTORY_SEARCH_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If the armour inventory contains less item stacks than have to be dropped, the game will additionally look at the main inventory (first) and the hotbar for armour item stacks.");
        extendHotbarInventorySearch = configuration.getBoolean (EXTEND_HOTBAR_INVENTORY_SEARCH_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If the hotbar contains less item stacks than have to be dropped, the game will additionally look at the main inventory for item stacks to be dropped.");
        extendMainInventorySearch = configuration.getBoolean (EXTEND_MAIN_INVENTORY_SEARCH_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If the main inventory contains less item stacks than have to be dropped, the game will additionally look at the hotbar for item stacks to be dropped.");
        allowPerPlayerSpying = configuration.getBoolean (PER_PLAYER_SPYING_SETTINGS_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, true,
            "If true, players can decide whether they want to enable spying or not. If enabled, they can see proximity informations of other players if they have PvP enabled, and also their proximity informations will be accessible.");
        warmupOff = configuration.getInt (WARMUP_ON_OFF_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, 300, 0,
            Integer.MAX_VALUE,
            "The delay after which the PvP mode of a player with PvP ON will be actually toggled (after initiating it).");
        showProximityDirection = configuration.getBoolean (SHOW_PROXIMITY_DIRECTION_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY,
            true,
            "Shows additionally to the proximity information the direction of the other players.");
        enablePartialInventoryLossPvE = configuration.getBoolean (PVE_PARTIAL_INVENTORY_LOSS_ENABLED_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If set to true, the partial inventory loss for PvE (Player versus Environment) will be enabled. All deaths caused by the environment (water, height, fire, plants, ...), and also NPCs, count as PvE. If keepInventory is enabled, the player will loose a specified amount of item stacks from his hotbar and armor and main inventory slots upon death caused by PvE. The item stacks will be dropped.");
        inventoryLossArmourPvE = configuration.getInt (PVE_ARMOUR_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            1, 0, 4,
            "The amount of item stacks from the armour inventory the player looses upon death caused by PvE.");
        inventoryLossHotbarPvE = configuration.getInt (PVE_HOTBAR_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            2, 0, 9,
            "The amount of item stacks from the hotbar the player looses upon death caused by PvE.");
        inventoryLossMainPvE = configuration.getInt (PVE_MAIN_ITEM_LOSS_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, 0, 0,
            27,
            "The amount of item stacks from the main inventory the player looses upon death caused by PvE.");
        allowIndirectPvP = configuration.getBoolean (ALLOW_INDIRECT_PVP_CONFIGURATION_NAME,
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If true, attacks from NPCs owned by a player (dogs, hired units, ...) will count as PvP and not as PvE.");
        prefixGlobalMessages = configuration.getBoolean ("",
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY,
            true,
            "If true, all global chat messages sent by the PvP Mode Mod will be prefixed with a configurable, global prefix.");
        globalMessagePrefix = configuration.getString (GLOBAT_CHAT_MESSAGE_PREFIX_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY,
            ServerChatUtils.DEFAULT_CHAT_MESSAGE_PREFIX,
            "The prefix appended to every global chat message (if prefixing is enabled). It must not be blank. You can also use the MC formatting codes to give the prefix a color.");
        pvpTogglingEnabled = configuration.getBoolean (PVP_TOGGLING_ENABLED_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, true,
            "If true, players can decide by themselves with /pvp whether they want to have PvP enabled or not (as long as no overrides apply).");
        defaultPvPMode = configuration.getBoolean (DEFAULT_PVP_MODE_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, false,
            "This specifies the default PvP mode players will have (true for PvP enabled, false for disabled).");
        forceDefaultPvPMode = configuration.getBoolean (FORCE_DEFAULT_PVP_MODE_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, false,
            "If true, the default PvP mode will be forced for all players. For that 'Enable PvP Toggling' has to be set to false. Admins can still override the PvP mode of a player with /pvpadmin, and the override conditions still overrude this setting.");
        announcePvPEnabledGlobally = configuration.getBoolean (ANNOUNCE_PVP_ENABLED_GLOBALLY_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY,
            true, "Sends a message to all players if PvP is enabled for a player.");
        announcePvPDisabledGlobally = configuration.getBoolean (ANNOUNCE_PVP_DISABLED_GLOBALLY_CONFIGURATION_NAME,
            CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY,
            false, "Sends a message to all players if PvP is disabled for a player.");

        configuration.addCustomCategoryComment (CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY,
            "Configuration entries related to the CSV combat logging handler");
        configuration.addCustomCategoryComment (
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            "Configuration entries related to the partial inventory loss");

        if (csvSeparator.length () != 1)
        {
            FMLLog.warning ("The csv separator \"%s\" is invalid. The default one will be used.",
                csvSeparator);
            csvSeparator = ";";
        }

        Iterator<String> commandIterator = commandBlacklist.iterator ();
        while (commandIterator.hasNext ())
        {
            String commandName = commandIterator.next ();
            if (commandName.trim ().isEmpty ())
            {
                commandIterator.remove ();
            }
        }

        if (globalMessagePrefix.trim ().isEmpty ())
        {
            globalMessagePrefix = ServerChatUtils.DEFAULT_CHAT_MESSAGE_PREFIX;
            FMLLog.warning ("The global chat message prefix is empty. A default one will be used.");
        }

        FMLLog.info ("%d commands are blacklisted", commandBlacklist.size ());

        if (ServerProxy.forceDefaultPvPMode && ServerProxy.pvpTogglingEnabled)
        {
            FMLLog.warning (
                "The configuration property 'Force Default PvP Mode' is set to true but 'Pvp Toggeling Enabled' is set to false, but required to be set to true.");
        }

        if (configuration.hasChanged ())
        {
            configuration.save ();
        }

        overrideManager = new OverrideManagerImpl ();
    }

    @Override
    protected void registerCompatibilityModules ()
    {
        super.registerCompatibilityModules ();
        compatibilityManager.registerModuleLoader (LOTRModCompatibilityModuleLoader.class);
        compatibilityManager.registerModuleLoader (SuffixForgeCompatibilityModuleLoader.class);
        compatibilityManager.registerModuleLoader (SiegeModeCompatiblityModuleLoader.class);
    }

    @Override
    public void onInit (FMLInitializationEvent event) throws Exception
    {
        super.onInit (event);

        combatLogManager.preInit ();

        String[] validPvPLogHandlerNames = combatLogManager.getRegisteredHandlerNames ();

        /*
         * We've to load this property in init because it depends on previously
         * registered handlers - other mods may register handlers in preinit.
         */
        activatedPvPLoggingHandlers = new HashSet<> (Arrays.asList (
            configuration.getStringList ("Active Pvp Logging Handlers",
                CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, new String[]
                {combatLogManager.getDefaultHandlerName ()},
                "Valid values: " + Arrays.toString (validPvPLogHandlerNames)
                    + ". Leave it empty (without empty lines!) to disable pvp logging.",
                validPvPLogHandlerNames)));

        if (activatedPvPLoggingHandlers.size () > 0)
        {
            Iterator<String> activatedHandlersIterator = activatedPvPLoggingHandlers.iterator ();
            while (activatedHandlersIterator.hasNext ())
            {
                String handlerName = activatedHandlersIterator.next ();
                if (!combatLogManager.isValidHandlerName (handlerName))
                {
                    FMLLog.warning ("The pvp combat logging handler \"%s\" is not valid.",
                        handlerName);
                    activatedHandlersIterator.remove ();
                }
            }
            if (activatedPvPLoggingHandlers.isEmpty ())
            {
                FMLLog.warning ("No valid pvp combat logging handlers were specified. A default one will be used");
                activatedPvPLoggingHandlers.add (combatLogManager.getDefaultHandlerName ());
            }
        }
        else
        {
            FMLLog.warning ("No pvp combat logging handlers were specified. PvP combat logging will be disabled.");
        }

        if (configuration.hasChanged ())
        {
            configuration.save ();
        }

        if (activatedPvPLoggingHandlers.size () > 0)
        {
            activatedPvPLoggingHandlers.forEach (combatLogManager::activateHandler);
            FMLLog.info ("Activated the following pvp combat logging handlers: %s", activatedPvPLoggingHandlers);
            combatLogManager.init (combatLogDir);
        }

        PvPServerEventHandler.init ();
    }

    @Override
    public void onPostInit (FMLPostInitializationEvent event) throws Exception
    {
        super.onPostInit (event);
    }

    public void onServerStarting (FMLServerStartingEvent event)
    {
        cfg = MinecraftServer.getServer ().getConfigurationManager ();

        pvpCommandInstance = new PvPCommand ();
        pvplistCommandInstance = new PvPCommandList ();
        pvpadminCommandInstance = new PvPCommandAdmin ();
        pvphelpCommandInstance = new PvPCommandHelp ();
        pvpconfigCommandInstance = new PvPCommandConfig ();

        event.registerServerCommand (pvpCommandInstance);
        event.registerServerCommand (pvplistCommandInstance);
        event.registerServerCommand (pvpadminCommandInstance);
        event.registerServerCommand (pvphelpCommandInstance);
        event.registerServerCommand (pvpconfigCommandInstance);
    }

    public void onServerStopping (FMLServerStoppingEvent event)
    {
        if (activatedPvPLoggingHandlers.size () > 0)
        {
            combatLogManager.close ();
        }
    }

}
