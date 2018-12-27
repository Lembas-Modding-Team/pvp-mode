package pvpmode.modules.suffixForge.internal.server;

import java.nio.file.Path;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.common.configuration.*;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;
import pvpmode.api.server.utils.PvPServerUtils;
import pvpmode.modules.suffixForge.api.server.SuffixForgeServerConfiguration;

/**
 * The compatibility module for SuffixForge.
 *
 * @author CraftedMods
 *
 */
public class SuffixForgeCompatibilityModule extends AbstractCompatibilityModule implements Configurable
{

    private SuffixForgeServerConfiguration config;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        config = this.createConfiguration (configFile ->
        {
            return new SuffixForgeServerConfigurationImpl (configFile, PvPMode.proxy.getAutoConfigManager ()
                .getGeneratedKeys ().get (SuffixForgeServerConfiguration.SUFFIX_FORGE_SERVER_CONFIG_PID), logger);
        });

    }

    @SubscribeEvent
    public void onPartialItemLoss (PartialItemLossEvent event)
    {
        if (!config.areSoulboundItemsDropped ())
        {
            event.setCanceled (PvPServerUtils.isSoulbound (event.getStack ())); // TODO: The soulbound feature is now
            // incorporated into the PvP Mode Mod. The
            // compatibiltiy module stays for
            // compatibility reasons, with 2.0.0-BETA it
            // will be removed.
        }
    }

    @Override
    public ConfigurationManager getConfiguration ()
    {
        return config;
    }

}
