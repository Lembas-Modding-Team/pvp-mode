package pvpmode.internal.server;

import java.nio.file.*;
import java.util.*;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.StringSet;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.compatibility.events.CombatLoggingHandlerRegistryEvent;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.log.LogHandlerConstants;
import pvpmode.api.server.utils.*;
import pvpmode.internal.common.CommonProxy;
import pvpmode.internal.server.command.*;
import pvpmode.internal.server.configuration.ServerConfigurationImpl;
import pvpmode.internal.server.log.*;
import pvpmode.internal.server.overrides.OverrideManagerImpl;
import pvpmode.internal.server.utils.*;
import pvpmode.modules.lotr.internal.server.LOTRModCompatibilityModuleLoader;
import pvpmode.modules.siegeMode.internal.server.SiegeModeCompatiblityModuleLoader;
import pvpmode.modules.suffixForge.internal.server.SuffixForgeCompatibilityModuleLoader;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "=" + ServerConfiguration.SERVER_CONFIG_PID)
public class ServerProxy extends CommonProxy
{

    private CombatLogManagerImpl combatLogManager;
    private OverrideManagerImpl overrideManager;
    private ServerConfigurationManager serverConfigurationManager;

    private Path combatLogDir;

    private PvPCommand pvpCommandInstance;
    private PvPCommandAdmin pvpadminCommandInstance;
    private PvPCommandConfig pvpconfigCommandInstance;
    private PvPCommandHelp pvphelpCommandInstance;
    private PvPCommandList pvplistCommandInstance;

    private PvPServerEventHandler eventHandler;

    private ServerChatUtilsProvider chatUtilsProvider;
    private PvPServerUtilsProvider serverUtilsProvider;

    public ServerProxy ()
    {
        super ();
        ServerChatUtils.setProvider (chatUtilsProvider = new ServerChatUtilsProvider (this));
        PvPServerUtils.setProvider (serverUtilsProvider = new PvPServerUtilsProvider (this));
    }

    @Override
    public void onPreInit (FMLPreInitializationEvent event) throws Exception
    {
        super.onPreInit (event);

        combatLogDir = Paths.get (event.getModConfigurationDirectory ().getParent ().toString (), "logs", "combat");

        Files.createDirectories (combatLogDir);

        combatLogManager = new CombatLogManagerImpl (LogHandlerConstants.CSV_CONFIG_NAME, combatLogDir);

        combatLogManager.registerCombatLogHandler (LogHandlerConstants.SIMPLE_CONFIG_NAME,
            new SimpleCombatLogHandler ());
        combatLogManager.registerCombatLogHandler (LogHandlerConstants.CSV_CONFIG_NAME, new CSVCombatLogHandler ());

        CombatLoggingHandlerRegistryEvent combatLogHandlerRegistryEvent = new CombatLoggingHandlerRegistryEvent ();
        PvPServerUtils.postEventAndGetResult (combatLogHandlerRegistryEvent,
            combatLogHandlerRegistryEvent::getRegisteredHandlers).forEach (combatLogManager::registerCombatLogHandler);

        combatLogManager.init ();

        configuration = new ServerConfigurationImpl (this, forgeConfiguration,
            this.getModifiedConfigurationPropertyKeys ());
        configuration.load ();

        overrideManager = new OverrideManagerImpl ();

        chatUtilsProvider.preInit ();
        serverUtilsProvider.preInit ();
    }

    @Override
    protected void registerCompatibilityModules ()
    {
        super.registerCompatibilityModules ();
        compatibilityManager.registerModuleLoader (LOTRModCompatibilityModuleLoader.class);
        compatibilityManager.registerModuleLoader (SuffixForgeCompatibilityModuleLoader.class);
        compatibilityManager.registerModuleLoader (SiegeModeCompatiblityModuleLoader.class);
    }

    private Map<String, ConfigurationPropertyKey<?>> getModifiedConfigurationPropertyKeys ()
    {
        Map<String, ConfigurationPropertyKey<?>> generatedKeys = autoConfigManager.getGeneratedKeys ()
            .get (ServerConfiguration.SERVER_CONFIG_PID);
        for (String internalName : generatedKeys.keySet ())
        {
            if (internalName.equals ("active_combat_logging_handlers"))
            {
                ConfigurationPropertyKey.StringSet key = (StringSet) generatedKeys.get (internalName);
                generatedKeys.replace (internalName,
                    new ConfigurationPropertyKey.StringSet (internalName, key.getCategory (),
                        key.getDefaultValue (),
                        new HashSet<> (Arrays.asList (combatLogManager.getRegisteredHandlerNames ()))));
            }
        }
        return generatedKeys;
    }

    @Override
    public void onInit (FMLInitializationEvent event) throws Exception
    {
        super.onInit (event);

        eventHandler = new PvPServerEventHandler ();

        MinecraftForge.EVENT_BUS.register (eventHandler);
        FMLCommonHandler.instance ().bus ().register (eventHandler);
    }

    @Override
    public void onPostInit (FMLPostInitializationEvent event) throws Exception
    {
        super.onPostInit (event);
    }

    public void onServerStarting (FMLServerStartingEvent event)
    {
        serverConfigurationManager = MinecraftServer.getServer ().getConfigurationManager ();

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
        if (this.getConfiguration ().getActiveCombatLoggingHandlers ().size () > 0)
        {
            combatLogManager.close ();
        }
    }

    @Override
    public ServerConfiguration getConfiguration ()
    {
        return (ServerConfiguration) super.getConfiguration ();
    }

    public CombatLogManagerImpl getCombatLogManager ()
    {
        return combatLogManager;
    }

    public OverrideManagerImpl getOverrideManager ()
    {
        return overrideManager;
    }

    public ServerConfigurationManager getServerConfigurationManager ()
    {
        return serverConfigurationManager;
    }

    public Collection<AbstractPvPCommand> getServerCommands ()
    {
        return Arrays.asList (pvpCommandInstance, pvpadminCommandInstance, pvpconfigCommandInstance,
            pvphelpCommandInstance, pvplistCommandInstance);
    }

}
