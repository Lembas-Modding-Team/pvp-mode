package pvpmode.api.server.network;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * An interface for bundling the data the server knows about the client. An
 * implementation should implement hashCode and equals, and only take the player
 * instance into account for these. This means, that two instances of this class
 * are seen as equal, if they have the same player.
 * 
 * @author CraftedMods
 *
 */
public interface ClientData
{

    /**
     * The entity instance of the player representing the client
     * 
     * @return The player instance
     */
    public EntityPlayerMP getPlayer ();

    /**
     * The version of the PvP Mode Mod that is installed client-side
     * 
     * @return The remote version
     */
    public String getRemoteVersion ();

    /**
     * A list of compatibility modules installed AND loaded client-side.
     * 
     * @return The client-side compatibility modules
     */
    public Collection<String> getLoadedCompatibilityModules ();

}
