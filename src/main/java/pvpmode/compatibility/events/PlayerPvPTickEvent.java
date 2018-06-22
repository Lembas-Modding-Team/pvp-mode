package pvpmode.compatibility.events;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;

/**
 * This event will be fired every tick while a player is in PvP
 * @author CraftedMods
 *
 */
public class PlayerPvPTickEvent extends Event
{

    private final EntityPlayer player;

    public PlayerPvPTickEvent (EntityPlayer player)
    {
        this.player = player;
    }

    /**
     * Returns the player which is in PvP
     */
    public EntityPlayer getPlayer ()
    {
        return player;
    }

}
