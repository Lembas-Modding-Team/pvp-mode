package pvpmode.compatibility.modules;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.compatibility.CompatibilityModule;
import pvpmode.compatibility.events.EntityMasterExtractionEvent;

/**
 * The compatibility module for the LOTR Mod.
 * @author CraftedMods
 *
 */
public class LOTRModCompatibilityModule implements CompatibilityModule
{
    @Override
    public void load ()
    {
        MinecraftForge.EVENT_BUS.register (this);
    }

    @SubscribeEvent
    public void onEntityMasterExtraction (EntityMasterExtractionEvent event)
    {
        Entity entity = event.getEntity ();
        if (entity instanceof LOTREntityNPC)
        {
            LOTREntityNPC npc = (LOTREntityNPC) entity;
            if (npc.hiredNPCInfo.isActive)
            {
                EntityPlayer master = npc.hiredNPCInfo.getHiringPlayer ();
                if (master != null)
                {
                    event.setMaster ((EntityPlayerMP) master);
                }
            }
        }

    }

}
