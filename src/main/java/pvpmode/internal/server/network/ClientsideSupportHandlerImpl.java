package pvpmode.internal.server.network;

import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import pvpmode.PvPMode;
import pvpmode.api.common.version.SemanticVersion;
import pvpmode.api.server.network.ClientsideSupportHandler;

/**
 * The implementation of the clientside support handler
 * 
 * @author CraftedMods
 *
 */
public class ClientsideSupportHandlerImpl implements ClientsideSupportHandler
{

    @Override
    public boolean isRemoteVersionSupported (String version)
    {
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

    private Set<UUID> supportedPlayers = new HashSet<> ();

    @Override
    public void addClientsideSupport (UUID player)
    {
        supportedPlayers.add (player);
    }

    @Override
    public void removeClientsideSupport (UUID player)
    {
        supportedPlayers.remove (player);
    }

    @Override
    public boolean isClientsideSupported (UUID player)
    {
        return supportedPlayers.contains (player);
    }

    @Override
    public void sendInitialSupportPackages (EntityPlayerMP player)
    {
        // TODO Currently not used
    }

}
