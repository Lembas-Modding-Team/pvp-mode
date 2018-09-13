package pvpmode.modules.suffixForge.internal.server;

import static pvpmode.modules.suffixForge.api.server.SuffixForgeServerConfigurationConstants.*;

import java.nio.file.Path;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;
import pvpmode.api.server.configuration.ServerConfiguration;

/**
 * The compatibility module for SuffixForge.
 *
 * @author CraftedMods
 *
 */
public class SuffixForgeCompatibilityModule extends AbstractCompatibilityModule
{

    private boolean partialInvLossDropSoulboundItems;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        Configuration configuration = this.getDefaultConfiguration ();

        partialInvLossDropSoulboundItems = configuration.getBoolean (
            DROP_SOULBOUND_ITEMS_CONFIGURATION_CATEGORY,
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
        if (!partialInvLossDropSoulboundItems)
        {
            ItemStack stack = event.getStack ();
            if (stack.hasTagCompound ())
            {
                if (stack.getTagCompound ().getBoolean ("SoulboundBool"))
                {
                    event.setCanceled (true);
                }
            }
        }
    }

}
