package pvpmode;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.common.config.Configuration;
import pvpmode.command.*;
import pvpmode.compatibility.CompatibilityManager;
import pvpmode.compatibility.modules.deathcraft.DeathcraftCompatibilityModuleLoader;
import pvpmode.compatibility.modules.enderio.EnderIOCompatibilityModuleLoader;
import pvpmode.compatibility.modules.lootableBodies.LootableBodiesCompatibilityModuleLoader;
import pvpmode.compatibility.modules.lotr.LOTRModCompatibilityModuleLoader;
import pvpmode.compatibility.modules.siegeMode.SiegeModeCompatiblityModuleLoader;
import pvpmode.compatibility.modules.suffixForge.SuffixForgeCompatibilityModuleLoader;
import pvpmode.log.*;
import pvpmode.overrides.PvPOverrideManager;

@Mod(modid = "pvp-mode", name = "PvP Mode", version = "1.5.0-BETA", acceptableRemoteVersions = "*")
public class PvPMode
{
    public static Configuration config;
    public static ServerConfigurationManager cfg;
    public static PvPCombatLogManager combatLogManager;
    public static CompatibilityManager compatibilityManager = new CompatibilityManager ();
    public static PvPOverrideManager overrideManager;

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

    public static boolean soulboundItemsEnabled;
    public static String soulboundTooltip;

    public static final String MAIN_CONFIGURATION_CATEGORY = "MAIN";
    public static final String CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY = "PVP_LOGGING_CSV";
    public static final String PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY = "PARTIAL_INVENTORY_LOSS";

    private Path combatLogDir;

    public static PvPCommand pvpCommandInstance;
    public static PvPCommandAdmin pvpadminCommandInstance;
    public static PvPCommandConfig pvpconfigCommandInstance;
    public static PvPCommandHelp pvphelpCommandInstance;
    public static PvPCommandList pvplistCommandInstance;
    public static SoulboundCommand soulboundCommandInstance;

    public static final String DEFAULT_SOULBOUND_TOOLTIP = "\u00A7r\u00A78[\u00A75Soulbound\u00A7r\u00A78]";

    @EventHandler
    public void preinit (FMLPreInitializationEvent event) throws IOException
    {
        combatLogDir = Paths.get (event.getModConfigurationDirectory ().getParent ().toString (), "logs", "combat");

        Files.createDirectories (combatLogDir);

        config = new Configuration (event.getSuggestedConfigurationFile ());
        combatLogManager = new PvPCombatLogManager (CSVCombatLogHandler.CONFIG_NAME);

        combatLogManager.registerCombatLogHandler (SimpleCombatLogHandler.CONFIG_NAME, new SimpleCombatLogHandler ());
        combatLogManager.registerCombatLogHandler (CSVCombatLogHandler.CONFIG_NAME, new CSVCombatLogHandler ());

        roundFactor = config.getInt ("Distance Rounding Factor", MAIN_CONFIGURATION_CATEGORY, 100, 1, Integer.MAX_VALUE,
            "The factor by which the proximity information will be rounded. The distance will be displayed in a multiple of this factor.");
        warmup = config.getInt ("Warmup (seconds)", MAIN_CONFIGURATION_CATEGORY, 30, 0, Integer.MAX_VALUE,
            "The delay after which the PvP mode of a player with PvP OFF will be actually toggled (after initiating it).");
        cooldown = config.getInt ("Cooldown (seconds)", MAIN_CONFIGURATION_CATEGORY, 900, 0, Integer.MAX_VALUE,
            "The duration after a PvP mode toggle while which the PvP mode cannot be toggled again.");
        radar = config.getBoolean ("Radar", MAIN_CONFIGURATION_CATEGORY, true,
            "If true, players with PvP mode enabled will receive proximity information about other players with PvP enabled. If allow per player spy settings is enabled this info will only be available for players who enabled spy.");
        csvSeparator = config.getString ("CSV separator", CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY,
            CSVCombatLogHandler.DEFAULT_CSV_SEPARATOR,
            "The separator character used between columns in the CSV file. Usually a semicolon or comma. Please note that in some countries the decimal separator is a comma. Decimal numbers will be written to the logs.")
            .trim ();
        partialInventoryLossEnabled = config.getBoolean ("Enable Partial Inventory Loss",
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            true,
            "If set to true, the partial inventory loss for PvP will be enabled. If keepInventory is enabled, the player will loose a specified amount of item stacks from his hotbar and armor and main inventory slots upon death caused by PvP. The item stacks will be dropped.");
        inventoryLossArmour = config.getInt ("Armour Item Loss", PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, 1, 0, 4,
            "The amount of item stacks from the armour inventory the player looses upon death caused by PvP.");
        inventoryLossHotbar = config.getInt ("Hotbar Item Loss", PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, 2, 0, 9,
            "The amount of item stacks from the hotbar the player looses upon death caused by PvP.");
        overrideCheckInterval = config.getInt ("PvP Mode Override Check Interval (Seconds)",
            MAIN_CONFIGURATION_CATEGORY, 10, -1, 60,
            "Specifies how often the mod checks for PvP mode overrides. If set to zero, the checks will be executed every tick. Set it to -1 to disable the PvP mode overrides.");
        pvpTimer = config.getInt ("PvP Timer (Seconds)", MAIN_CONFIGURATION_CATEGORY, 45, 10, 300,
            "Specifies the time interval after a combat event while which all involved players are seen as \"in PvP\".");
        commandBlacklist = new HashSet<> (Arrays.asList (
            config.getStringList ("Command Blacklist", MAIN_CONFIGURATION_CATEGORY, new String[] {},
                "Commands in this list cannot be executed by players who are in PvP. Note that this only applies for commands which are registered on the server. The commands are specified by the command name, without the slash. Invalid command names will be ignored.")));
        inventoryLossMain = config.getInt ("Main Item Loss", PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, 0, 0, 27,
            "The amount of item stacks from the main inventory the player looses upon death caused by PvP.");
        blockShiftClicking = config.getBoolean ("Disable Fast Item Transfer",
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, false,
            "If true, players won't be able to transfer item stacks in their inventory with shift-clicking while in PvP");
        extendArmourInventorySearch = config.getBoolean ("Extend Armour Inventory Search",
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If the armour inventory contains less item stacks than have to be dropped, the game will additionally look at the main inventory (first) and the hotbar for armour item stacks.");
        extendHotbarInventorySearch = config.getBoolean ("Extend Hotbar Inventory Search",
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If the hotbar contains less item stacks than have to be dropped, the game will additionally look at the main inventory for item stacks to be dropped.");
        extendMainInventorySearch = config.getBoolean ("Extend Main Inventory Search",
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If the main inventory contains less item stacks than have to be dropped, the game will additionally look at the hotbar for item stacks to be dropped.");
        allowPerPlayerSpying = config.getBoolean ("Allow Per Player Spying Settings", MAIN_CONFIGURATION_CATEGORY, true,
            "If true, players can decide whether they want to enable spying or not. If enabled, they can see proximity informations of other players if they have PvP enabled, and also their proximity informations will be accessible.");
        warmupOff = config.getInt ("Warmup on-off (seconds)", MAIN_CONFIGURATION_CATEGORY, 300, 0, Integer.MAX_VALUE,
            "The delay after which the PvP mode of a player with PvP ON will be actually toggled (after initiating it).");
        showProximityDirection = config.getBoolean ("Show Proximity Direction", MAIN_CONFIGURATION_CATEGORY, true,
            "Shows additionally to the proximity information the direction of the other players.");
        enablePartialInventoryLossPvE = config.getBoolean ("Enable Partial Inventory Loss For PvE",
            PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If set to true, the partial inventory loss for PvE (Player versus Environment) will be enabled. All deaths caused by the environment (water, height, fire, plants, ...), and also NPCs, count as PvE. If keepInventory is enabled, the player will loose a specified amount of item stacks from his hotbar and armor and main inventory slots upon death caused by PvE. The item stacks will be dropped.");
        inventoryLossArmourPvE = config.getInt ("PvE Armour Item Loss", PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            1, 0, 4,
            "The amount of item stacks from the armour inventory the player looses upon death caused by PvE.");
        inventoryLossHotbarPvE = config.getInt ("PvE Hotbar Item Loss", PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            2, 0, 9,
            "The amount of item stacks from the hotbar the player looses upon death caused by PvE.");
        inventoryLossMainPvE = config.getInt ("PvE Main Item Loss", PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, 0, 0,
            27,
            "The amount of item stacks from the main inventory the player looses upon death caused by PvE.");
        allowIndirectPvP = config.getBoolean ("Allow Indirect PvP", PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY, true,
            "If true, attacks from NPCs owned by a player (dogs, hired units, ...) will count as PvP and not as PvE.");
        prefixGlobalMessages = config.getBoolean ("Prefix Global Chat Messages", MAIN_CONFIGURATION_CATEGORY, true,
            "If true, all global chat messages sent by the PvP Mode Mod will be prefixed with a configurable, global prefix.");
        globalMessagePrefix = config.getString ("Global Chat Message Prefix", MAIN_CONFIGURATION_CATEGORY,
            ChatUtils.DEFAULT_CHAT_MESSAGE_PREFIX,
            "The prefix appended to every global chat message (if prefixing is enabled). It must not be blank. You can also use the MC formatting codes to give the prefix a color.");
        pvpTogglingEnabled = config.getBoolean ("Enable PvP Toggling", MAIN_CONFIGURATION_CATEGORY, true,
            "If true, players can decide by themselves with /pvp whether they want to have PvP enabled or not (as long as no overrides apply).");
        defaultPvPMode = config.getBoolean ("Default PvP Mode", MAIN_CONFIGURATION_CATEGORY, false,
            "This specifies the default PvP mode players will have (true for PvP enabled, false for disabled).");
        forceDefaultPvPMode = config.getBoolean ("Force Default PvP Mode", MAIN_CONFIGURATION_CATEGORY, false,
            "If true, the default PvP mode will be forced for all players. For that 'Enable PvP Toggling' has to be set to false. Admins can still override the PvP mode of a player with /pvpadmin, and the override conditions still overrude this setting.");
        announcePvPEnabledGlobally = config.getBoolean ("Announce PvP Enabled Globally", MAIN_CONFIGURATION_CATEGORY,
            true, "Sends a message to all players if PvP is enabled for a player.");
        announcePvPDisabledGlobally = config.getBoolean ("Announce PvP Disabled Globally", MAIN_CONFIGURATION_CATEGORY,
            false, "Sends a message to all players if PvP is disabled for a player.");

        soulboundItemsEnabled = config.getBoolean ("Enable Soulbound Items",
            MAIN_CONFIGURATION_CATEGORY, true,
            "If true, the soulbound command of the PvP Mode Mod will be enabled and items marked as soulbound won't be dropped. If SuffixForge is present, the soulbound command will be disabled.");
        soulboundTooltip = config.getString ("Soulbound Item Tooltip", MAIN_CONFIGURATION_CATEGORY,
            DEFAULT_SOULBOUND_TOOLTIP,
            "The tooltip shown for soulbound items. It musn't be blank. You can use the MC formatting codes to format the tooltip.");

        config.addCustomCategoryComment (MAIN_CONFIGURATION_CATEGORY, "General configuration entries");
        config.addCustomCategoryComment (CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY,
            "Configuration entries related to the CSV combat logging handler");
        config.addCustomCategoryComment (PARTIAL_INVENTORY_LOSS_CONFIGURATION_CATEGORY,
            "Configuration entries related to the partial inventory loss");

        if (csvSeparator.length () != 1)
        {
            FMLLog.warning ("The csv separator \"%s\" is invalid. The default one will be used.",
                csvSeparator);
            csvSeparator = CSVCombatLogHandler.DEFAULT_CSV_SEPARATOR;
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
            globalMessagePrefix = ChatUtils.DEFAULT_CHAT_MESSAGE_PREFIX;
            FMLLog.warning ("The global chat message prefix is empty. A default one will be used.");
        }

        FMLLog.info ("%d commands are blacklisted", commandBlacklist.size ());

        if (PvPMode.forceDefaultPvPMode && PvPMode.pvpTogglingEnabled)
        {
            FMLLog.warning (
                "The configuration property 'Force Default PvP Mode' is set to true but 'Pvp Toggeling Enabled' is set to false, but required to be set to true.");
        }

        if (Loader.isModLoaded ("suffixforge"))
        {
            soulboundItemsEnabled = false;
            FMLLog.info ("SuffixForge is present - the soulbound command will be disabled");
        }

        if (soulboundTooltip.trim ().isEmpty ())
        {
            soulboundTooltip = DEFAULT_SOULBOUND_TOOLTIP;
            FMLLog.warning ("The soulbound tooltip is empty. A default one will be used.");
        }

        if (config.hasChanged ())
        {
            config.save ();
        }

        overrideManager = new PvPOverrideManager ();

        registerDefaultCompatibilityModules ();
    }

    private void registerDefaultCompatibilityModules ()
    {
        compatibilityManager.registerModuleLoader (LOTRModCompatibilityModuleLoader.class);
        compatibilityManager.registerModuleLoader (SuffixForgeCompatibilityModuleLoader.class);
        compatibilityManager.registerModuleLoader (SiegeModeCompatiblityModuleLoader.class);
        compatibilityManager.registerModuleLoader (EnderIOCompatibilityModuleLoader.class);
        compatibilityManager.registerModuleLoader (DeathcraftCompatibilityModuleLoader.class);
        compatibilityManager.registerModuleLoader (LootableBodiesCompatibilityModuleLoader.class);
    }

    @EventHandler
    public void init (FMLInitializationEvent event) throws IOException

    {

        combatLogManager.preInit ();

        String[] validPvPLogHandlerNames = combatLogManager.getRegisteredHandlerNames ();

        /*
         * We've to load this property in init because it depends on previously
         * registered handlers - other mods may register handlers in preinit.
         */
        activatedPvPLoggingHandlers = new HashSet<> (Arrays.asList (
            config.getStringList ("Active Pvp Logging Handlers", MAIN_CONFIGURATION_CATEGORY, new String[]
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

        if (config.hasChanged ())
        {
            config.save ();
        }

        if (activatedPvPLoggingHandlers.size () > 0)
        {
            activatedPvPLoggingHandlers.forEach (combatLogManager::activateHandler);
            FMLLog.info ("Activated the following pvp combat logging handlers: %s", activatedPvPLoggingHandlers);
            combatLogManager.init (combatLogDir);
        }

        PvPEventHandler.init ();
    }

    @EventHandler
    public void serverLoad (FMLServerStartingEvent event)
    {
        cfg = MinecraftServer.getServer ().getConfigurationManager ();

        pvpCommandInstance = new PvPCommand ();
        pvplistCommandInstance = new PvPCommandList ();
        pvpadminCommandInstance = new PvPCommandAdmin ();
        pvphelpCommandInstance = new PvPCommandHelp ();
        pvpconfigCommandInstance = new PvPCommandConfig ();
        soulboundCommandInstance = new SoulboundCommand ();

        event.registerServerCommand (pvpCommandInstance);
        event.registerServerCommand (pvplistCommandInstance);
        event.registerServerCommand (pvpadminCommandInstance);
        event.registerServerCommand (pvphelpCommandInstance);
        event.registerServerCommand (pvpconfigCommandInstance);

        if (soulboundItemsEnabled)
        {
            event.registerServerCommand (soulboundCommandInstance);// TODO: Can be done with the compatibility module
        }

        if (!compatibilityManager.areModulesLoaded ())
        {
            compatibilityManager.loadRegisteredModules ();
        }

        if (PvPMode.config.hasChanged ())
        {
            PvPMode.config.save (); // Save the configs of the compatibility modules
        }
    }

    @EventHandler
    public void serverClose (FMLServerStoppingEvent event)
    {
        if (activatedPvPLoggingHandlers.size () > 0)
        {
            combatLogManager.close ();
        }
    }
}
