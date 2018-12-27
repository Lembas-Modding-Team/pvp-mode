package pvpmode.modules.enderio.internal.server;

import java.lang.reflect.*;
import java.nio.file.Path;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import crazypants.enderio.enchantment.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.common.configuration.*;
import pvpmode.api.server.compatibility.events.PartialItemLossEvent;
import pvpmode.modules.enderio.api.server.EnderIOServerConfiguration;

/**
 * The compatibility module for Ender IO.
 *
 * @author CraftedMods
 *
 */
public class EnderIOCompatibilityModule extends AbstractCompatibilityModule implements Configurable
{

    private EnderIOServerConfiguration config;

    private boolean partialInvLossDropSoulboundItems;

    private EnchantmentSoulBound soulboundEnchantment;

    private Method isSoulboundStackMethod;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        config = this.createConfiguration (configFile ->
        {
            return new EnderIOServerConfigurationImpl (configFile, PvPMode.proxy.getAutoConfigManager ()
                .getGeneratedKeys ().get (EnderIOServerConfiguration.ENDER_IO_SERVER_CONFIG_PID), logger);
        });

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
                logger.errorThrowable ("Couldn't check whether the current item is an Ender IO soulbound one", e);
            }
        }
    }

    @Override
    public ConfigurationManager getConfiguration ()
    {
        return config;
    }

}
