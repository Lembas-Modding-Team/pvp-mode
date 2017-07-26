package pvpmode;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityCreature;
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
		
		if (event.source.getEntity () == null)
			return;
		
		boolean pvpDenied =
				(event.entityLiving.getEntityData ().getBoolean ("PvPDenied")
				&& (event.source.getEntity () instanceof EntityPlayerMP
				|| (event.source.getEntity () instanceof EntityCreature
				&& PvPMode.blockCreatureDamage)))
				|| event.source.getEntity ().getEntityData ().getBoolean ("PvPDenied");
		
		if (pvpDenied)
			event.setCanceled (true);
	}
	
	public static void init ()
	{
		INSTANCE = new PvPEventHandler ();
		MinecraftForge.EVENT_BUS.register (INSTANCE);
	}

}
