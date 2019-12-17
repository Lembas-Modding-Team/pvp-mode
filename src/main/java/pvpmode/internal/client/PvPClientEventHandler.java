package pvpmode.internal.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import pvpmode.PvPMode;
import pvpmode.internal.common.network.ClientsideFeatureSupportRequest;

public class PvPClientEventHandler
{

    @SubscribeEvent
    public void onPlayerLoggedIn (EntityJoinWorldEvent event)
    {
        if (event.entity == Minecraft.getMinecraft ().thePlayer)
        {
            PvPMode.proxy.getPacketDispatcher ()
                .sendToServer (new ClientsideFeatureSupportRequest (PvPMode.VERSION,
                    getLoadedCompatibilitylModuleInternalNames ()));
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut (ClientDisconnectionFromServerEvent event)
    {
        PvPMode.instance.getClientProxy ().clearCachedServerData ();
    }

    private String[] getLoadedCompatibilitylModuleInternalNames ()
    {
        return PvPMode.proxy.getCompatibilityManager ().getLoadedModules ().keySet ().stream ()
            .map (loader -> loader.getInternalModuleName ())
            .toArray (size -> new String[size]);
    }

}
