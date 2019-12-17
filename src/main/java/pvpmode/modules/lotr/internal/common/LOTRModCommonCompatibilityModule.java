package pvpmode.modules.lotr.internal.common;

import java.nio.file.Path;

import cpw.mods.fml.relauncher.Side;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.internal.common.CommonProxy;
import pvpmode.modules.lotr.internal.common.gear.CommonBlockedGearManager;
import pvpmode.modules.lotr.internal.common.network.*;
import pvpmode.modules.lotr.internal.common.network.BlockedGearItemsListChangedMessage.BlockedGearItemsListChangedMessageHandler;
import pvpmode.modules.lotr.internal.common.network.EquippingOfBlockedArmorBlockedConfigurationChangeMessage.EquippingOfBlockedArmorBlockedConfigurationChangeMessageHandler;
import pvpmode.modules.lotr.internal.common.network.GearItemsBlockedConfigurationChange.GearItemsBlockedConfigurationChangeHandler;

/**
 * An abstract, common base class for the compatibility modules for the LOTR
 * Mod.
 *
 * @author CraftedMods
 *
 */
public abstract class LOTRModCommonCompatibilityModule extends AbstractCompatibilityModule
{

    protected CommonBlockedGearManager blockedGearManager;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        PvPMode.proxy.getPacketDispatcher ().registerMessage (GearItemsBlockedConfigurationChangeHandler.class,
            GearItemsBlockedConfigurationChange.class, CommonProxy.getNextPacketId (), Side.CLIENT);
        PvPMode.proxy.getPacketDispatcher ().registerMessage (BlockedGearItemsListChangedMessageHandler.class,
            BlockedGearItemsListChangedMessage.class, CommonProxy.getNextPacketId (), Side.CLIENT);
        PvPMode.proxy.getPacketDispatcher ().registerMessage (
            EquippingOfBlockedArmorBlockedConfigurationChangeMessageHandler.class,
            EquippingOfBlockedArmorBlockedConfigurationChangeMessage.class, CommonProxy.getNextPacketId (),
            Side.CLIENT);
    }

    public CommonBlockedGearManager getBlockedGearManager ()
    {
        return blockedGearManager;
    }

}
