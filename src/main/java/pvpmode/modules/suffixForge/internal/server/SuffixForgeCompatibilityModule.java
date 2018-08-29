package pvpmode.modules.suffixForge.internal.server;

import static pvpmode.modules.suffixForge.api.server.SuffixForgeServerConfigurationConstants.*;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.api.common.compatibility.CompatibilityModule;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;
import pvpmode.api.server.utils.PvPServerUtils;
import pvpmode.internal.common.CommonProxy;

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
    public void load () throws Exception
    {
        MinecraftForge.EVENT_BUS.register (this);

        partialInvLossDropSoulboundItems = CommonProxy.configuration.getBoolean (
            DROP_SOULBOUND_ITEMS_CONFIGURATION_NAME,
            SUFFIX_FORGE_CONFIGURATION_CATEGORY,
            false, "If true, items tagged with soulbound can be dropped with the partial inventory loss.");

        CommonProxy.configuration.addCustomCategoryComment (SUFFIX_FORGE_CONFIGURATION_CATEGORY,
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
