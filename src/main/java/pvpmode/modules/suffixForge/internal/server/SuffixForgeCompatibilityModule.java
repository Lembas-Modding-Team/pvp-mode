package pvpmode.modules.suffixForge.internal.server;

import java.nio.file.Path;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.utils.PvPServerUtils;
import pvpmode.modules.suffixForge.api.server.SuffixForgeServerConfigurationConstants;

/**
 * The compatibility module for SuffixForge.
 *
 * @author CraftedMods
 *
 */
public class SuffixForgeCompatibilityModule extends AbstractCompatibilityModule
{

    private boolean soulboundItemsDropped;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        Configuration configuration = this.getDefaultConfiguration ();

        soulboundItemsDropped = configuration.getBoolean (
            SuffixForgeServerConfigurationConstants.SOULBOUND_ITEMS_DROPPED_CONFIGURATION_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY,
            false, "If true, items tagged with soulbound can be dropped with the partial inventory loss.");

        if (configuration.hasChanged ())
        {
            configuration.save ();
        }

    }

    @SubscribeEvent
    public void onPartialItemLoss (PartialItemLossEvent event)
    {
        if (!soulboundItemsDropped)
        {
            event.setCanceled (PvPServerUtils.isSoulbound (event.getStack ())); // TODO: The soulbound feature is now
            // incorporated into the PvP Mode Mod. The
            // compatibiltiy module stays for
            // compatibility reasons, with 2.0.0-BETA it
            // will be removed.
        }
    }

}
