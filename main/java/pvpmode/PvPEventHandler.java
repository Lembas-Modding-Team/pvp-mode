package pvpmode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.passive.EntityWolf;
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
		
		Entity attacker = event.source.getEntity ();
		Entity victim = event.entity;
		
		if (isEntityDirectlyProhibited (attacker))
			if (isEntityEligiblyProhibited (victim))
				event.setCanceled (true);
		
		if (isEntityDirectlyProhibited (victim))
			if (isEntityEligiblyProhibited (attacker))
				event.setCanceled (true);
	}
	
	public boolean isEntityEligiblyProhibited (Entity entity)
	{
		//Null pointers result in "instanceof" evaluating false.

		return entity instanceof EntityPlayerMP || getMaster (entity) instanceof EntityPlayerMP;
	}
	
	public boolean isEntityDirectlyProhibited (Entity entity)
	{
		if (entity.getEntityData ().getBoolean ("PvPDenied"))
			return true;
		
		if (getMaster (entity) != null)
			if (getMaster (entity).getEntityData ().getBoolean ("PvPDenied"))
				return true;
		
		return false;
	}
	
	public EntityPlayerMP getMaster (Entity entity)
	{
		if (entity instanceof EntityWolf)
			return (EntityPlayerMP)((EntityWolf)entity).getOwner ();
		
		//LOTR patch begins.
		
		Class entityClass = entity.getClass ();
		
		try
		{
			//Only Dumbledore and I are capable of this kind of magic.
			Field hiredUnitInfo = entityClass.getField ("hiredNPCInfo");
			Class hiredClass = hiredUnitInfo.getClass ();
			Method getHiringPlayer = hiredClass.getMethod ("getHiringPlayer", new Class[] {});
			
			Object o = getHiringPlayer.invoke (hiredUnitInfo.get (entity), new Object[] {});
			
			if (o instanceof EntityPlayerMP)
				return (EntityPlayerMP)o;
			
			return null;
		}
		catch (NoSuchFieldException ex)
		{
			//This entity is not a LOTR unit.
			return null;
		}
		catch (NoSuchMethodException ex)
		{
			//Something changed with the LOTR mod.
			return null;
		}
		catch (IllegalAccessException ex)
		{
			//Hopefully impossible since I am only accessing public fields/methods.
			return null;
		}
		catch (InvocationTargetException ex)
		{
			//No idea why this would occur.
			return null;
		}
	}
	
	public static void init ()
	{
		INSTANCE = new PvPEventHandler ();
		MinecraftForge.EVENT_BUS.register (INSTANCE);
	}

}
