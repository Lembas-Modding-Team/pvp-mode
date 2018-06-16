package pvpmode.compatibility.events;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

public class EntityMasterExtractionEvent extends Event
{

    private final Entity entity;
    private EntityPlayerMP master;

    public EntityMasterExtractionEvent (Entity entity)
    {
        this.entity = entity;
    }

    public Entity getEntity ()
    {
        return entity;
    }

    public EntityPlayerMP getMaster ()
    {
        return master;
    }

    public void setMaster (EntityPlayerMP master)
    {
        this.master = master;
    }

}
