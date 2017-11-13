package pvpmode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PvPEventHandler
{
    public static PvPEventHandler INSTANCE;

    ChatComponentText disabled = new ChatComponentText (
        EnumChatFormatting.RED + "This player/unit has PvP disabled!");

    /**
     * The workhorse method of the whole mod.
     */
    @SubscribeEvent
    public void interceptPvP (LivingAttackEvent event)
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

        if (isPreventable (attacker))
            if (isPrevented (victim))
                event.setCanceled (true);

        if (isPreventable (victim))
            if (isPrevented (attacker))
                event.setCanceled (true);

        if (!event.isCanceled ())
        {
            // PvP toggle warmups are cancelled.

            if (victim instanceof EntityPlayerMP)
                victim.getEntityData ().setLong ("PvPTime", 0);

            if (attacker instanceof EntityPlayerMP)
                attacker.getEntityData ().setLong ("PvPTime", 0);
        }
        else if (attacker instanceof EntityPlayerMP)
            ((EntityPlayerMP) attacker).addChatComponentMessage (disabled);
    }

    /**
     * Handles PvP warmup timers.
     */
    @SubscribeEvent
    public void onLivingUpdate (LivingUpdateEvent event)
    {
        EntityPlayerMP player;

        if (event.entityLiving instanceof EntityPlayerMP)
            player = (EntityPlayerMP) event.entityLiving;
        else return;

        // The time at which the player's PvP status will be toggled.
        // 0 if the player is not currently in warmup.
        long pvptime = player.getEntityData ().getLong ("PvPTime");

        if (pvptime != 0 && pvptime < MinecraftServer.getSystemTimeMillis ())
        {
            // Reset the toggle time to "no warmup" state.
            player.getEntityData ().setLong ("PvPTime", 0);

            ServerConfigurationManager cfg = MinecraftServer.getServer ().getConfigurationManager ();

            if (player.getEntityData ().getBoolean ("PvPDenied"))
            {
                player.getEntityData ().setBoolean ("PvPDenied", false);

                // Warn the whole server.
                cfg.sendChatMsg (new ChatComponentText (EnumChatFormatting.RED
                    + "PvP is now enabled for " + player.getDisplayName ()));
            }
            else
            {
                player.getEntityData ().setBoolean ("PvPDenied", true);
                player.addChatComponentMessage (new ChatComponentText (
                    EnumChatFormatting.GREEN + "PvP is now disabled for you."));
            }
        }
    }

    /**
     * Determines if PvP blocking could ever be applied to this entity.
     */
    public boolean isPreventable (Entity entity)
    {
        // Null pointers result in "instanceof" evaluating false.

        return entity instanceof EntityPlayerMP || getMaster (entity) instanceof EntityPlayerMP;
    }

    /**
     * Determines if PvP blocking should be applied to this entity.
     */
    public boolean isPrevented (Entity entity)
    {
        if (entity.getEntityData ().getBoolean ("PvPDenied"))
            return true;

        EntityPlayerMP master = getMaster (entity);

        if (master != null)
            if (master.getEntityData ().getBoolean ("PvPDenied"))
                return true;

        return false;
    }

    /**
     * Returns the player that this entity is associated with, if possible.
     */
    public EntityPlayerMP getMaster (Entity entity)
    {
        if (entity instanceof EntityWolf)
            return (EntityPlayerMP) ((EntityWolf) entity).getOwner ();

        // LOTR patch begins.

        Class entityClass = entity.getClass ();

        try
        {
            // Only Dumbledore and I are capable of this kind of magic.
            // And even then it requires a silken hand and a subtle touch.
            Field hiredUnitInfo = entityClass.getField ("hiredNPCInfo");
            Class hiredClass = hiredUnitInfo.getType ();
            Method getHiringPlayer = hiredClass.getMethod ("getHiringPlayer", new Class[] {});

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

    public static void init ()
    {
        INSTANCE = new PvPEventHandler ();
        MinecraftForge.EVENT_BUS.register (INSTANCE);
    }

}
