package pvpmode.modules.suffixForge.internal.server;

import static pvpmode.modules.suffixForge.api.server.SuffixForgeServerConfigurationConstants.*;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import pvpmode.PvPMode;
import pvpmode.api.common.compatibility.CompatibilityModule;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;

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

        Configuration configuration = PvPMode.instance.getServerProxy ().getConfiguration ();

        partialInvLossDropSoulboundItems = configuration.getBoolean (
            DROP_SOULBOUND_ITEMS_CONFIGURATION_CATEGORY,
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
