package pvpmode.compatibility.modules.suffixForge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.PvPMode;
import pvpmode.compatibility.CompatibilityModule;
import pvpmode.compatibility.events.PartialItemLossEvent;

/**
 * The compatibility module for SuffixForge.
 *
 * @author CraftedMods
 *
 */
public class SuffixForgeCompatibilityModule implements CompatibilityModule
{

    private static final String SF_CONFIGURATION_CATEGORY = "SUFFIX_FORGE_COMPATIBILITY";

    private boolean partialInvLossDropSoulboundItems;

    @Override
    public void load () throws Exception
    {
        MinecraftForge.EVENT_BUS.register (this);

        partialInvLossDropSoulboundItems = PvPMode.config.getBoolean ("Drop Soulbound Items", SF_CONFIGURATION_CATEGORY,
            false, "If true, items tagged with soulbound can be dropped with the partial inventory loss.");

        PvPMode.config.addCustomCategoryComment (SF_CONFIGURATION_CATEGORY,
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
