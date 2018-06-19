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
import pvpmode.compatibility.modules.LOTRModCompatibilityModuleLoader;
import pvpmode.log.*;

@Mod(modid = "pvp-mode", name = "PvP Mode", version = "1.1.0-BETA.1", acceptableRemoteVersions = "*")
public class PvPMode
{
    public static Configuration config;
    public static ServerConfigurationManager cfg;
    public static PvPCombatLogManager combatLogManager;
    public static CompatibilityManager compatibilityManager = new CompatibilityManager ();

    public static int roundFactor;
    public static int warmup;
    public static int cooldown;
    public static boolean radar;
    public static Collection<String> activatedPvPLoggingHandlers;
    public static String csvSeparator;
    public static int inventoryLossArmour;
    public static int inventoryLossHotbar;

    public static final String MAIN_CONFIGURATION_CATEGORY = "MAIN";
    public static final String CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY = "PVP_LOGGING_CSV";

    private Path combatLogDir;

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
            "");
        warmup = config.getInt ("Warmup (seconds)", MAIN_CONFIGURATION_CATEGORY, 300, 0, Integer.MAX_VALUE, "");
        cooldown = config.getInt ("Cooldown (seconds)", MAIN_CONFIGURATION_CATEGORY, 900, 0, Integer.MAX_VALUE, "");
        radar = config.getBoolean ("Radar", MAIN_CONFIGURATION_CATEGORY, true, "");
        csvSeparator = config.getString ("CSV separator", CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY,
            CSVCombatLogHandler.DEFAULT_CSV_SEPARATOR,
            "The separator character used between columns in the CSV file. Usually a semicolon or comma. Please note that in some countries the decimal separator is a comma. Decimal numbers will be written to the logs.")
            .trim ();
        inventoryLossArmour = config.getInt ("Armour Item Loss", MAIN_CONFIGURATION_CATEGORY, 1, 0, 4,
            "The amount of items from the armour inventory the player looses upon death. This only applies if keeyInventory is true.");
        inventoryLossHotbar = config.getInt ("Hotbar Item Loss", MAIN_CONFIGURATION_CATEGORY, 2, 0, 9,
            "The amount of items from the hotbar the player looses upon death. This only applies if keeyInventory is true.");

        config.addCustomCategoryComment (MAIN_CONFIGURATION_CATEGORY, "General configuration entries");
        config.addCustomCategoryComment (CSV_COMBAT_LOGGING_CONFIGURATION_CATEGORY,
            "Configuration entries related to the CSV combat logging handler");

        if (csvSeparator.length () != 1)
        {
            FMLLog.warning ("The csv separator \"%s\" is invalid. The default one will be used.",
                csvSeparator);
            csvSeparator = CSVCombatLogHandler.DEFAULT_CSV_SEPARATOR;
        }

        if (config.hasChanged ())
            config.save ();

        registerDefaultCompatibilityModules ();
    }

    private void registerDefaultCompatibilityModules ()
    {
        compatibilityManager.registerModuleLoader (LOTRModCompatibilityModuleLoader.class);
    }

    @EventHandler
    public void init (FMLInitializationEvent event) throws IOException

    {

        compatibilityManager.loadRegisteredModules ();
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
            config.save ();

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
        event.registerServerCommand (new PvPCommand ());
        event.registerServerCommand (new PvPCommandList ());
        event.registerServerCommand (new PvPCommandAdmin ());
        event.registerServerCommand (new PvPCommandHelp ());
        event.registerServerCommand (new PvPCommandConfig ());
    }

    @EventHandler
    public void serverClose (FMLServerStoppingEvent event)
    {
        if (activatedPvPLoggingHandlers.size () > 0)
            combatLogManager.close ();
    }
}
