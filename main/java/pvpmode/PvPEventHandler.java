package pvpmode;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class PvPEventHandler
{
	public static PvPEventHandler INSTANCE;
	
	@SubscribeEvent
	public void interceptPvP (LivingAttackEvent event)
	{
		/*If the PvPDenied NBT tag is not set, it can be treated as false. It will be created
		 * as soon as a player uses the /pvp command for the first time.
		 */
		
		if (event.entityLiving instanceof EntityPlayerMP) //if a player is whacked...
			if (event.source.getEntity () instanceof EntityPlayerMP) //by another player...
			{
				if(event.entityLiving.getEntityData ().getBoolean ("PvPDenied")) //if the victim is protected...
					event.setCanceled (true);
				else if (event.source.getEntity ().getEntityData ().getBoolean ("PvPDenied")) //or if the offender is prohibited...
					event.setCanceled (true); //DENIED!
			}
	}
	
	public static void init ()
	{
		INSTANCE = new PvPEventHandler ();
		MinecraftForge.EVENT_BUS.register (INSTANCE);
	}

}
