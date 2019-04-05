package pvpmode.internal.server.network;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayerMP;
import pvpmode.api.server.network.ClientData;

/**
 * A simple implementation of the ClientData interface.
 * 
 * @author CraftedMods
 *
 */
public class ClientDataImpl implements ClientData
{

    private final EntityPlayerMP player;
    private final String remoteVersion;
    private final Collection<String> loadedCompatibilityModules;

    public ClientDataImpl (EntityPlayerMP player, String remoteVersion, Collection<String> loadedCompatibilityModules)
    {
        this.player = player;
        this.remoteVersion = remoteVersion;
        this.loadedCompatibilityModules = loadedCompatibilityModules;
    }

    @Override
    public EntityPlayerMP getPlayer ()
    {
        return player;
    }

    @Override
    public String getRemoteVersion ()
    {
        return remoteVersion;
    }

    @Override
    public Collection<String> getLoadedCompatibilityModules ()
    {
        return loadedCompatibilityModules;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( (player == null) ? 0 : player.hashCode ());
        return result;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass () != obj.getClass ())
            return false;
        ClientDataImpl other = (ClientDataImpl) obj;
        if (player == null)
        {
            if (other.player != null)
                return false;
        }
        else if (!player.equals (other.player))
            return false;
        return true;
    }

    @Override
    public String toString ()
    {
        return String.format (
            "ClientData: [username=\"%s\", uuid=\"%s\", remoteVersion=\"%s\", loadedCompatibilityModules=\"%s\"]",
            player.getDisplayName (), player.getUniqueID ().toString (), remoteVersion, loadedCompatibilityModules);
    }
}
