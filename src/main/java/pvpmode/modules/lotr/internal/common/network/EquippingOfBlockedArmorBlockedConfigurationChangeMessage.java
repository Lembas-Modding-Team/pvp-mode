package pvpmode.modules.lotr.internal.common.network;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.modules.lotr.internal.client.LOTRModClientCompatibilityModule;
import pvpmode.modules.lotr.internal.common.LOTRModCompatibilityModuleLoader;

public class EquippingOfBlockedArmorBlockedConfigurationChangeMessage implements IMessage
{

    private boolean isEquippingOfBlockedArmorBlocked;

    public EquippingOfBlockedArmorBlockedConfigurationChangeMessage ()
    {
    }

    public EquippingOfBlockedArmorBlockedConfigurationChangeMessage (boolean isEquippingOfBlockedArmorBlocked)
    {
        this.isEquippingOfBlockedArmorBlocked = isEquippingOfBlockedArmorBlocked;
    }

    @Override
    public void fromBytes (ByteBuf buf)
    {
        isEquippingOfBlockedArmorBlocked = buf.readBoolean ();
    }

    @Override
    public void toBytes (ByteBuf buf)
    {
        buf.writeBoolean (isEquippingOfBlockedArmorBlocked);
    }

    public static class EquippingOfBlockedArmorBlockedConfigurationChangeMessageHandler
        implements
        IMessageHandler<EquippingOfBlockedArmorBlockedConfigurationChangeMessage, EquippingOfBlockedArmorBlockedConfigurationChangeMessage>
    {

        @Override
        public EquippingOfBlockedArmorBlockedConfigurationChangeMessage onMessage (
            EquippingOfBlockedArmorBlockedConfigurationChangeMessage message,
            MessageContext ctx)
        {
            PvPCommonUtils.executeForCompatibilityModule (LOTRModCompatibilityModuleLoader.class,
                LOTRModClientCompatibilityModule.class, (loader, module) ->
                {
                    module.getBlockedGearManager ()
                        .setEquippingOfBlockedArmorBlocked (message.isEquippingOfBlockedArmorBlocked);
                });
            return null;
        }

    }

}
