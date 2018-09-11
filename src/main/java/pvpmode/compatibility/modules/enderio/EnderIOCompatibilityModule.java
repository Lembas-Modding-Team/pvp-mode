package pvpmode.compatibility.modules.enderio;

import java.lang.reflect.*;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import crazypants.enderio.enchantment.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.PvPMode;
import pvpmode.compatibility.CompatibilityModule;
import pvpmode.compatibility.events.PartialItemLossEvent;

/**
 * The compatibility module for Ender IO.
 *
 * @author CraftedMods
 *
 */
public class EnderIOCompatibilityModule implements CompatibilityModule
{

    private static final String ENDER_IO_CONFIGURATION_CATEGORY = "ENDER_IO_COMPATIBILITY";

    private boolean partialInvLossDropSoulboundItems;

    private EnchantmentSoulBound soulboundEnchantment;

    private Method isSoulboundStackMethod;

    @Override
    public void load () throws Exception
    {
        MinecraftForge.EVENT_BUS.register (this);

        partialInvLossDropSoulboundItems = PvPMode.config.getBoolean ("Drop Soulbound Items",
            ENDER_IO_CONFIGURATION_CATEGORY,
            false, "If true, items tagged with soulbound can be dropped with the partial inventory loss.");

        PvPMode.config.addCustomCategoryComment (ENDER_IO_CONFIGURATION_CATEGORY,
            "Configuration entries for compatibility with the \"Ender IO\" Mod");

        Enchantments enchantments = Enchantments.getInstance ();

        Field souldBoundEnchantmentField = enchantments.getClass ().getDeclaredField ("soulBound");
        souldBoundEnchantmentField.setAccessible (true);

        soulboundEnchantment = (EnchantmentSoulBound) souldBoundEnchantmentField.get (enchantments);

        isSoulboundStackMethod = soulboundEnchantment.getClass ().getDeclaredMethod ("isSoulBound", ItemStack.class);
        isSoulboundStackMethod.setAccessible (true);

    }

    @SubscribeEvent
    public void onPartialItemLoss (PartialItemLossEvent event)
    {
        if (!partialInvLossDropSoulboundItems)
        {
            ItemStack stack = event.getStack ();

            try
            {
                if ((boolean) isSoulboundStackMethod.invoke (soulboundEnchantment, stack))
                {
                    event.setCanceled (true);
                }
            }
            catch (Exception e)
            {
                FMLLog.getLogger ().error ("Couldn't check whether the current item is an Ender IO soulbound one", e);
            }
        }
    }

}
