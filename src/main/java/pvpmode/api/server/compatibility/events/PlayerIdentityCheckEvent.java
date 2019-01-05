package pvpmode.api.server.compatibility.events;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.entity.player.EntityPlayerMP;
import pvpmode.api.server.utils.PvPServerUtils;

/**
 * An event called when
 * {@link PvPServerUtils#getMaster(net.minecraft.entity.Entity)} is called. Some
 * mods/plugins seem to use player entities, even if they don't represent a real player.
 * Because of that the PvP Mode Mod has to determine whether the entity is a
 * real player or not. If this has to be determined, this event will be fired,
 * if it's canceled, the supplied player is not a real one.
 * 
 * @author CraftedMods
 *
 */
@Cancelable
public class PlayerIdentityCheckEvent extends Event
{

    private final EntityPlayerMP player;

    public PlayerIdentityCheckEvent (EntityPlayerMP player)
    {
        this.player = player;
    }

    /**
     * The player to test
     */
    public EntityPlayerMP getPlayer ()
    {
        return player;
    }

}
