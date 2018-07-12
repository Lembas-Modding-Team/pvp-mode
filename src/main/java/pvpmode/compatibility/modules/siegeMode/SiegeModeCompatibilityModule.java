package pvpmode.compatibility.modules.siegeMode;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.PvPMode;
import pvpmode.compatibility.CompatibilityModule;
import pvpmode.compatibility.events.*;
import pvpmode.compatibility.events.PvPListEvent.UnsafeClassification;
import siege.common.siege.SiegeDatabase;

/**
 * The compatibility module for the Siege Mode Mod.
 *
 * @author CraftedMods
 *
 */
public class SiegeModeCompatibilityModule implements CompatibilityModule
{

    private static final String SIEGE_MODE_CONFIGURATION_CATEGORY = "SIEGE_MODE_MOD_COMPATIBILITY";

    private boolean disablePvPLoggingDuringSieges;

    @Override
    public void load () throws Exception
    {
        MinecraftForge.EVENT_BUS.register (this);

        disablePvPLoggingDuringSieges = PvPMode.config.getBoolean ("Disable PvP Logging During Sieges",
            SIEGE_MODE_CONFIGURATION_CATEGORY, true, "If true, PvP events for all players of a siege won't be logged.");

        PvPMode.config.addCustomCategoryComment (SIEGE_MODE_CONFIGURATION_CATEGORY,
            "Configuration entries for compatibility with the \"Siege Mode\" Mod");

        PvPMode.overrideManager.registerOverrideCondition (new SiegeZoneOverrideCondition ());
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
        if (disablePvPLoggingDuringSieges && (SiegeDatabase.getActiveSiegeForPlayer (event.getAttacker ()) != null
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
