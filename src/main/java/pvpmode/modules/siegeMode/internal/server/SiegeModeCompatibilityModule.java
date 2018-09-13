package pvpmode.modules.siegeMode.internal.server;

import static pvpmode.modules.siegeMode.api.server.SiegeModeServerConfigurationConstants.*;

import java.nio.file.Path;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.server.compatibility.events.*;
import pvpmode.api.server.compatibility.events.PvPListEvent.UnsafeClassification;
import pvpmode.api.server.configuration.ServerConfiguration;
import siege.common.siege.SiegeDatabase;

/**
 * The compatibility module for the Siege Mode Mod.
 *
 * @author CraftedMods
 *
 */
public class SiegeModeCompatibilityModule extends AbstractCompatibilityModule
{

    private boolean pvpLoggingDuringSiegesDisabled;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        Configuration configuration = this.getDefaultConfiguration ();

        pvpLoggingDuringSiegesDisabled = configuration.getBoolean (
            PVP_LOGGING_DURING_SIEGES_DISABLED_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, true,
            "If true, PvP events for all players of a siege won't be logged.");

        if (configuration.hasChanged ())
        {
            configuration.save ();
        }

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
        if (pvpLoggingDuringSiegesDisabled && (SiegeDatabase.getActiveSiegeForPlayer (event.getAttacker ()) != null
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

}
