package pvpmode.modules.enderio.internal.server;

import java.lang.reflect.*;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import crazypants.enderio.enchantment.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.api.common.compatibility.CompatibilityModule;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;
import pvpmode.internal.server.ServerProxy;
import pvpmode.modules.enderio.api.server.EnderIOServerConfigurationConstants;

/**
 * The compatibility module for Ender IO.
 *
 * @author CraftedMods
 *
 */
public class EnderIOCompatibilityModule implements CompatibilityModule
{

    private boolean partialInvLossDropSoulboundItems;

    private EnchantmentSoulBound soulboundEnchantment;

    private Method isSoulboundStackMethod;

    @Override
    public void load () throws Exception
    {
        MinecraftForge.EVENT_BUS.register (this);

        partialInvLossDropSoulboundItems = ServerProxy.configuration.getBoolean (
            EnderIOServerConfigurationConstants.DROP_SOULBOUND_ITEMS_CONFIGURATION_NAME,
            EnderIOServerConfigurationConstants.ENDER_IO_CONFIGURATION_CATEGORY,
            false, "If true, items tagged with soulbound can be dropped with the partial inventory loss.");

        ServerProxy.configuration.addCustomCategoryComment (
            EnderIOServerConfigurationConstants.ENDER_IO_CONFIGURATION_CATEGORY,
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
