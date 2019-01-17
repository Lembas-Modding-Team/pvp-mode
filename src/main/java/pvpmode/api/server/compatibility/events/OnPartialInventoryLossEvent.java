package pvpmode.api.server.compatibility.events;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

/**
 * An event fired every time before the partial inventory loss applies. If
 * canceled, the partial inventory loss won't apply for the player.
 *
 * @author CraftedMods
 *
 */
@Cancelable
public class OnPartialInventoryLossEvent extends Event
{

    private final EntityPlayer player;
    private final DamageSource source;

    public OnPartialInventoryLossEvent (EntityPlayer player, DamageSource source)
    {
        this.player = player;
        this.source = source;
    }

    /**
     * Returns the player who died
     */
    public EntityPlayer getPlayer ()
    {
        return player;
    }

    /**
     * Returns the damage source
     */
    public DamageSource getSource ()
    {
        return source;
    }

}
