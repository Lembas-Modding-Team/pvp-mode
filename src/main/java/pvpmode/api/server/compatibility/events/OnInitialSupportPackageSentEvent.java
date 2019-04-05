package pvpmode.api.server.compatibility.events;

import cpw.mods.fml.common.eventhandler.Event;
import pvpmode.api.server.network.*;

/**
 * Fired when
 * {@link ClientsideSupportHandler#sendInitialSupportPackages(net.minecraft.entity.player.EntityPlayerMP)}
 * is called. Can be used to send additional packages to the client.
 * 
 * @author CraftedMods
 *
 */
public class OnInitialSupportPackageSentEvent extends Event
{

    private final ClientData clientData;

    public OnInitialSupportPackageSentEvent (ClientData clientData)
    {
        this.clientData = clientData;
    }

    /**
     * Returns the client data of the specified client.
     * 
     * @return The client data
     */
    public ClientData getClientData ()
    {
        return clientData;
    }

}
