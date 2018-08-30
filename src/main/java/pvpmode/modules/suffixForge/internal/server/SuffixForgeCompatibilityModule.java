package pvpmode.modules.suffixForge.internal.server;

import static pvpmode.modules.suffixForge.api.server.SuffixForgeServerConfigurationConstants.SUFFIX_FORGE_CONFIGURATION_CATEGORY;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.CompatibilityModule;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;
import pvpmode.api.server.utils.PvPServerUtils;
import pvpmode.modules.suffixForge.api.server.SuffixForgeServerConfigurationConstants;

/**
 * The compatibility module for SuffixForge.
 *
 * @author CraftedMods
 *
 */
public class SuffixForgeCompatibilityModule implements CompatibilityModule
{

    private boolean partialInvLossDropSoulboundItems;

    @Override
    public void load (SimpleLogger logger) throws Exception
    {
        MinecraftForge.EVENT_BUS.register (this);

        Configuration configuration = PvPMode.instance.getServerProxy ().getConfiguration ();

        partialInvLossDropSoulboundItems = configuration.getBoolean (
           SuffixForgeServerConfigurationConstants.DROP_SOULBOUND_ITEMS_CONFIGURATION_NAME,
            SUFFIX_FORGE_CONFIGURATION_CATEGORY,
            false, "If true, items tagged with soulbound can be dropped with the partial inventory loss.");

        configuration.addCustomCategoryComment (SUFFIX_FORGE_CONFIGURATION_CATEGORY,
            "Configuration entries for compatibility with the \"Suffix Forge\" Mod");

    }

    @SubscribeEvent
    public void onPartialItemLoss (PartialItemLossEvent event)
    {
        if (!partialInvLossDropSoulboundItems)
        {
                event.setCanceled (PvPServerUtils.isSoulbound (event.getStack ())); // TODO: The soulbound feature is now
                                                                              // incorporated into the PvP Mode Mod. The
                                                                              // compatibiltiy module stays for
                                                                              // compatibility reasons, with 2.0.0-BETA it
                                                                              // will be removed.
        }
    }

}
