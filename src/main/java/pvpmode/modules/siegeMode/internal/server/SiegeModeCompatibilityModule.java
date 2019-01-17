package pvpmode.modules.siegeMode.internal.server;

import java.nio.file.Path;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.common.configuration.*;
import pvpmode.api.server.compatibility.events.*;
import pvpmode.api.server.compatibility.events.PvPListEvent.UnsafeClassification;
import pvpmode.modules.siegeMode.api.server.SiegeModeServerConfiguration;
import siege.common.siege.SiegeDatabase;

/**
 * The compatibility module for the Siege Mode Mod.
 *
 * @author CraftedMods
 *
 */
public class SiegeModeCompatibilityModule extends AbstractCompatibilityModule implements Configurable
{

    private SiegeModeServerConfiguration config;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        config = this.createConfiguration (configFile ->
        {
            return new SiegeModeServerConfigurationImpl (configFile, PvPMode.proxy.getAutoConfigManager ()
                .getGeneratedKeys ().get (SiegeModeServerConfiguration.SIEGE_MODE_SERVER_CONFIG_PID), logger);
        });

        PvPMode.instance.getServerProxy ().getOverrideManager ()
            .registerOverrideCondition (new SiegeZoneOverrideCondition ());
    }

    @SubscribeEvent
    public void onPartialInventoryLoss (OnPartialInventoryLossEvent event)
    {
        if (SiegeDatabase.getActiveSiegeForPlayer (event.getPlayer ()) != null)
        {
            event.setCanceled (true);
        }
    }

    @SubscribeEvent
    public void onPvPLog (OnPvPLogEvent event)
    {
        if (config.isPvPLoggingDuringSiegesDisabled ()
            && (SiegeDatabase.getActiveSiegeForPlayer (event.getAttacker ()) != null
                || SiegeDatabase.getActiveSiegeForPlayer (event.getVictim ()) != null))
        {
            event.setCanceled (true);
        }
    }

    @SubscribeEvent
    public void onUnsafePriorization (UnsafeClassification event)
    {
        if (SiegeDatabase.getActiveSiegeForPlayer (event.getConsumer ()) != null
            || SiegeDatabase.getActiveSiegeForPlayer (event.getProvider ()) != null)
        {
            event.setCanceled (true);
        }
    }

    @Override
    public ConfigurationManager getConfiguration ()
    {
        return config;
    }

}
