package pvpmode.api.common.compatibility.events;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.entity.player.EntityPlayer;
import pvpmode.api.common.utils.PvPCommonUtils;

/**
 * An event called when
 * {@link PvPCommonUtils#getPlayer(net.minecraft.entity.Entity)} is called. Some
 * mods/plugins seem to use player entities, even if they don't represent a real
 * player. Because of that, the PvP Mode Mod has to determine whether the entity
 * is a real player or not. If this has to be determined, this event will be
 * fired, if it's canceled, the supplied player isn't considered as real.
 * 
 * @author CraftedMods
 *
 */
@Cancelable
public class PlayerIdentityCheckEvent extends Event
{

    private final EntityPlayer player;

    public PlayerIdentityCheckEvent (EntityPlayer player)
    {
        this.player = player;
    }

    /**
     * The player to test
     */
    public EntityPlayer getPlayer ()
    {
        return player;
    }

}
