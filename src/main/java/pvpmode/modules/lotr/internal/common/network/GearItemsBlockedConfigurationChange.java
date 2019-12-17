package pvpmode.modules.lotr.internal.common.network;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.modules.lotr.internal.client.LOTRModClientCompatibilityModule;
import pvpmode.modules.lotr.internal.common.LOTRModCompatibilityModuleLoader;

public class GearItemsBlockedConfigurationChange implements IMessage
{

    private boolean areGearItemsBlocked;

    public GearItemsBlockedConfigurationChange ()
    {
    }

    public GearItemsBlockedConfigurationChange (boolean areGearItemsBlocked)
    {
        this.areGearItemsBlocked = areGearItemsBlocked;
    }

    @Override
    public void fromBytes (ByteBuf buf)
    {
        areGearItemsBlocked = buf.readBoolean ();
    }

    @Override
    public void toBytes (ByteBuf buf)
    {
        buf.writeBoolean (areGearItemsBlocked);
    }

    public static class GearItemsBlockedConfigurationChangeHandler
        implements IMessageHandler<GearItemsBlockedConfigurationChange, GearItemsBlockedConfigurationChange>
    {

        @Override
        public GearItemsBlockedConfigurationChange onMessage (GearItemsBlockedConfigurationChange message,
            MessageContext ctx)
        {
            PvPCommonUtils.executeForCompatibilityModule (LOTRModCompatibilityModuleLoader.class,
                LOTRModClientCompatibilityModule.class, (loader, module) ->
                {
                    module.getBlockedGearManager ().setAreGearItemsBlockedServerside (message.areGearItemsBlocked);
                });
            return null;
        }

    }

}
