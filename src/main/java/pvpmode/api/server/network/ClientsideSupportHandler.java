package pvpmode.api.server.network;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

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
     * Returns whether the remote version (the client's version) of the PvP Mode Mod
     * is supported.
     * 
     * @param version
     *            The client's version
     * @return Whether it's supported
     */
    public boolean isRemoteVersionSupported (String version);

    /**
     * Registers the specified client as supported.
     * 
     * @param player
     *            The UUID of the player
     */
    public void addClientsideSupport (UUID player);

    /**
     * Unregisters the specified client as supported.
     * 
     * @param player
     *            The UUID of the player
     */
    public void removeClientsideSupport (UUID player);

    /**
     * Returns whether the specified client is supported.
     * 
     * @param player
     *            The player's UUID
     * @return Whether the client is supported
     */
    public boolean isClientsideSupported (UUID player);

    /**
     * Called one time when a supported client connects. Used to broadcast the
     * packets containing the initial, relevant data to the client.
     * 
     * @param player
     *            The player
     */
    public void sendInitialSupportPackages (EntityPlayerMP player);

}
