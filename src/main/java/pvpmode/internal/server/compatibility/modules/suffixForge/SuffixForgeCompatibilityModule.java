package pvpmode.internal.server.compatibility.modules.suffixForge;

import static pvpmode.api.server.compatibility.events.modules.suffixForge.SuffixForgeServerConfigurationConstants.*;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.api.common.compatibility.CompatibilityModule;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;
import pvpmode.internal.server.ServerProxy;

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

        partialInvLossDropSoulboundItems = ServerProxy.configuration.getBoolean (DROP_SOULBOUND_ITEMS_CONFIGURATION_CATEGORY,
            SUFFIX_FORGE_CONFIGURATION_CATEGORY,
            false, "If true, items tagged with soulbound can be dropped with the partial inventory loss.");

        ServerProxy.configuration.addCustomCategoryComment (SUFFIX_FORGE_CONFIGURATION_CATEGORY,
            "Configuration entries for compatibility with the \"Suffix Forge\" Mod");

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
