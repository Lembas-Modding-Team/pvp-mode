package pvpmode.internal.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import pvpmode.PvPMode;
import pvpmode.internal.common.network.ClientsideFeatureSupportRequest;

public class PvPClientEventHandler
{

    @SubscribeEvent
    public void onPlayerLoggedIn (EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityPlayer)
        {
            PvPMode.proxy.getPacketDispatcher ()
                .sendToServer (new ClientsideFeatureSupportRequest (PvPMode.VERSION,
                    getLoadedCompatibilitylModuleInternalNames ()));
        }
    }

    private String[] getLoadedCompatibilitylModuleInternalNames ()
    {
        return PvPMode.proxy.getCompatibilityManager ().getLoadedModules ().keySet ().stream ()
            .map (loader -> loader.getInternalModuleName ())
            .toArray (size -> new String[size]);
    }

}
