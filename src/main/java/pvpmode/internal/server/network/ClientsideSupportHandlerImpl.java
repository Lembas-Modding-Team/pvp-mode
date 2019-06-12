package pvpmode.internal.server.network;

import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.PvPMode;
import pvpmode.api.common.version.SemanticVersion;
import pvpmode.api.server.compatibility.events.OnInitialSupportPackageSentEvent;
import pvpmode.api.server.network.*;

/**
 * The implementation of the client-side support handler
 * 
 * @author CraftedMods
 *
 */
public class ClientsideSupportHandlerImpl implements ClientsideSupportHandler
{

    private Map<UUID, ClientData> supportedClients = new HashMap<> ();

    @Override
    public boolean addSupportedClient (ClientData client)
    {
        if (!supportedClients.containsKey (client.getPlayer ().getUniqueID ()))
        {
            if (!checkClientData (client))
                return false;
            supportedClients.put (client.getPlayer ().getUniqueID (), client);
        }
        return true;
    }

    private boolean checkClientData (ClientData clientData)
    {
        String version = clientData.getRemoteVersion ();

        try
        {
            SemanticVersion remoteVersion = SemanticVersion.of (version);
            SemanticVersion localVersion = PvPMode.SEMANTIC_VERSION;

            return remoteVersion.getMajorVersion () == localVersion.getMajorVersion ()
                && remoteVersion.isPreRelease () == localVersion.isPreRelease ();
        }
        catch (IllegalArgumentException e)
        {
            // No semantic version
            return false;
        }

    }

    @Override
    public void removeSupportedClient (EntityPlayerMP player)
    {
        supportedClients.remove (player.getUniqueID ());
    }

    @Override
    public ClientData getClientData (EntityPlayerMP player)
    {
        return supportedClients.get (player.getUniqueID ());
    }

    @Override
    public boolean isClientSupported (EntityPlayerMP player)
    {
        return supportedClients.containsKey (player.getUniqueID ());
    }

    @Override
    public Collection<ClientData> getSupportedClients ()
    {
        return supportedClients.values ();
    }

    @Override
    public void sendInitialSupportPackages (EntityPlayerMP player)
    {
        MinecraftForge.EVENT_BUS
            .post (new OnInitialSupportPackageSentEvent (supportedClients.get (player.getUniqueID ())));
    }

}
