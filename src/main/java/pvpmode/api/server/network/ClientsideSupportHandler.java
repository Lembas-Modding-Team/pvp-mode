package pvpmode.api.server.network;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayerMP;
import pvpmode.api.server.compatibility.events.OnInitialSupportPackageSentEvent;

/**
 * Monitors the clients, that are supported currently. This handler will also
 * supply the initial data to the client.
 * 
 * @author CraftedMods
 *
 */
public interface ClientsideSupportHandler
{
    /**
     * Registers the specified client as supported, if it can be supported. This
     * method can execute checks to determine if the support is possible. Returns
     * false if the client can't be supported, otherwise true.
     * 
     * @param client
     *            The client to register
     * @return Whether the client will be supported
     * 
     */
    public boolean addSupportedClient (ClientData client);

    /**
     * Unregisters the client represented the specified player.
     * 
     * @param player
     *            The player representing the client
     */
    public void removeSupportedClient (EntityPlayerMP player);

    /**
     * Returns whether the client represented by the specified player is supported.
     * 
     * @param player
     *            The player representing the client
     * @return Whether the client is supported
     */
    public boolean isClientSupported (EntityPlayerMP player);

    /**
     * Returns the client data assigned to the specified player, or null, if there
     * aren't any data about that player.
     * 
     * @param player
     *            The player representing the client
     * @return The client data or null
     */
    public ClientData getClientData (EntityPlayerMP player);

    /**
     * Returns a collection containing all currently supported clients.
     * 
     * @return A collection with the supported clients
     */
    public Collection<ClientData> getSupportedClients ();

    /**
     * Called one time when a supported client connects. Used to broadcast the
     * packets containing the initial, relevant data to the client. Also sends the
     * {@link OnInitialSupportPackageSentEvent} event.
     * 
     * @param player
     *            The player
     */
    public void sendInitialSupportPackages (EntityPlayerMP player);

}
