package pvpmode.compatibility.events;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * This event will be fired if PvPMode has to determine the master of an
 * attacking entity.
 * 
 * @author CraftedMods
 *
 */
public class EntityMasterExtractionEvent extends Event
{

    private final Entity entity;
    private EntityPlayerMP master;

    public EntityMasterExtractionEvent (Entity entity)
    {
        this.entity = entity;
    }

    /**
     * Returns the entity which master should be determined
     */
    public Entity getEntity ()
    {
        return entity;
    }

    /**
     * Returns the currently determined master
     */
    public EntityPlayerMP getMaster ()
    {
        return master;
    }

    /**
     * Sets the determined master. This will replace the current value.
     */
    public void setMaster (EntityPlayerMP master)
    {
        this.master = master;
    }

}
