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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PvPEventHandler
{
    public static PvPEventHandler INSTANCE;

    @SubscribeEvent
    public void interceptPvP(LivingAttackEvent event)
    {
        /*
         * If the PvPDenied NBT tag is not set, it can be treated as false. It
         * will be created as soon as a player uses the /pvp command for the
         * first time.
         */

        Entity attacker = event.source.getEntity ();
        Entity victim = event.entity;

        if (attacker == null)
            return;

        if (isEntityDirectlyProhibited (attacker))
            if (isEntityEligiblyProhibited (victim))
                event.setCanceled (true);

        if (isEntityDirectlyProhibited (victim))
            if (isEntityEligiblyProhibited (attacker))
                event.setCanceled (true);
        
        if (!event.isCanceled ())
        {
            if (victim instanceof EntityPlayerMP)
                victim.getEntityData ().setLong ("PvPTime", 0);
            
            if (attacker instanceof EntityPlayerMP)
                attacker.getEntityData ().setLong ("PvPTime", 0);
        }
        else
            if (attacker instanceof EntityPlayerMP)
                ((EntityPlayerMP)attacker).addChatComponentMessage (new ChatComponentText (EnumChatFormatting.RED + "This player/unit has PvP disabled!"));
    }

    @SubscribeEvent
	public void onLivingUpdate (LivingUpdateEvent event)
	{
		EntityPlayerMP player;
		
		if (event.entityLiving instanceof EntityPlayerMP)
			player = (EntityPlayerMP)event.entityLiving;
		else return;
		
		long pvptime = player.getEntityData ().getLong ("PvPTime");
		
		if (pvptime != 0 && pvptime < MinecraftServer.getSystemTimeMillis ())
		{
		    player.getEntityData ().setLong ("PvPTime", 0);
		    
		    if (player.getEntityData ().getBoolean ("PvPDenied"))
		    {
		        player.getEntityData ().setBoolean ("PvPDenied", false);
		        player.addChatComponentMessage (new ChatComponentText (EnumChatFormatting.RED + "PvP is now enabled."));
		    }
		    else
		    {
		        player.getEntityData ().setBoolean ("PvPDenied", true);
		        player.addChatComponentMessage (new ChatComponentText (EnumChatFormatting.GREEN + "PvP is now disabled"));
		    }
		}
	}

    public boolean isEntityEligiblyProhibited(Entity entity)
    {
        // Null pointers result in "instanceof" evaluating false.

        return entity instanceof EntityPlayerMP || getMaster (entity) instanceof EntityPlayerMP;
    }

    public boolean isEntityDirectlyProhibited(Entity entity)
    {
        if (entity.getEntityData ().getBoolean ("PvPDenied"))
            return true;

        if (getMaster (entity) != null)
            if (getMaster (entity).getEntityData ().getBoolean ("PvPDenied"))
                return true;

        return false;
    }

    public EntityPlayerMP getMaster(Entity entity)
    {
        if (entity instanceof EntityWolf)
            return (EntityPlayerMP) ((EntityWolf) entity).getOwner ();

        // LOTR patch begins.

        Class entityClass = entity.getClass ();
        Field hiredUnitInfo = null;
        Class hiredClass;
        Method getHiringPlayer;

        try
        {
            // Only Dumbledore and I are capable of this kind of magic.
        	// And even then it requires a silken hand and a subtle touch.
            hiredUnitInfo = entityClass.getField ("hiredNPCInfo");
            hiredClass = hiredUnitInfo.getType ();
            getHiringPlayer = hiredClass.getMethod ("getHiringPlayer", new Class[] {});

            Object o = getHiringPlayer.invoke (hiredUnitInfo.get (entity), new Object[] {});

            if (o instanceof EntityPlayerMP)
                return (EntityPlayerMP) o;

            return null;
        }
        catch (NoSuchFieldException ex)
        {
            // This entity is not a LOTR unit.
            return null;
        }
        catch (NoSuchMethodException ex)
        {
            // Something changed with the LOTR mod.
            return null;
        }
        catch (IllegalAccessException ex)
        {
            // Hopefully impossible since I am only accessing public
            // fields/methods.
            FMLLog.info ("Security exception in PvPEventHandler at " + entityClass, null);
            return null;
        }
        catch (InvocationTargetException ex)
        {
            // No idea why this would occur.
            FMLLog.info ("InvocationTargetException thrown: " + ex.getCause ().getMessage (), null);
            return null;
        }
    }

    public static void init()
    {
        INSTANCE = new PvPEventHandler ();
        MinecraftForge.EVENT_BUS.register (INSTANCE);
    }

}
