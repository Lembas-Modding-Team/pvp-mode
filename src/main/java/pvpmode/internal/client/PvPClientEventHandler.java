package pvpmode.internal.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import pvpmode.PvPMode;
import pvpmode.api.common.network.ClientsideFeatureSupportRequest;

public class PvPClientEventHandler
{

    public PvPClientEventHandler ()
    {

    }

    @SubscribeEvent
    public void onPlayerLoggedIn (EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityPlayer)
        {
            PvPMode.proxy.getPacketDispatcher ().sendToServer (new ClientsideFeatureSupportRequest (PvPMode.VERSION));
        }
    }

}
