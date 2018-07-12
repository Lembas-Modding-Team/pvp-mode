package pvpmode.compatibility.events;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.entity.player.EntityPlayer;

/**
 * A basic event class for pvplist-related events
 *
 * @author CraftedMods
 *
 */
public abstract class PvPListEvent extends Event
{

    private final EntityPlayer consumer;
    private final EntityPlayer provider;

    public PvPListEvent (EntityPlayer consumer, EntityPlayer provider)
    {
        this.consumer = consumer;
        this.provider = provider;
    }

    /**
     * Returns the player who gets the pvplist
     */
    public EntityPlayer getConsumer ()
    {
        return consumer;
    }

    /**
     * Returns the player who is displayed in the pvplist
     */
    public EntityPlayer getProvider ()
    {
        return provider;
    }

    /**
     * An event which will be fired for every player while the proximity
     * informations will be computed. If canceled, the consumer player won't receive
     * proximity informations from the provider player.
     *
     * @author CraftedMods
     *
     */
    @Cancelable
    public static class ProximityVisibility extends PvPListEvent
    {

        public ProximityVisibility (EntityPlayer consumer, EntityPlayer provider)
        {
            super (consumer, provider);
        }

    }

    /**
     * An event which will be fired for every player determined as "unsafe" (which
     * means that PvP is enabled for that player). If this event is canceled, the
     * player will be seen as unsafe, but with a lower priority, which means, that
     * that player will be displayed further down on the list.
     *
     * @author CraftedMods
     *
     */
    @Cancelable
    public static class UnsafeClassification extends PvPListEvent
    {
        public UnsafeClassification (EntityPlayer consumer, EntityPlayer provider)
        {
            super (consumer, provider);
        }
    }

}
