package pvpmode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PvPEventHandler
{
    public static PvPEventHandler INSTANCE;

    /**
     * Cancels combat events associated with PvP-disabled players.
     */
    @SubscribeEvent
    public void interceptPvP (LivingAttackEvent event)
    {
        EntityPlayerMP attacker = getMaster (event.source.getEntity ());
        EntityPlayerMP victim = getMaster (event.entity);

        if (attacker == null || victim == null)
            return;

        NBTTagCompound attackerData = PvPUtils.getPvPData (attacker);
        NBTTagCompound victimData = PvPUtils.getPvPData (victim);

        if (attacker.capabilities.allowFlying)
        {
            if (attacker == event.source.getEntity ())
                fly (attacker);

            event.setCanceled (true);
            return;
        }

        if (victim.capabilities.allowFlying)
        {
            event.setCanceled (true);
            return;
        }

        if (attacker.capabilities.isCreativeMode)
        {
            if (attacker == event.source.getEntity ())
                gm1 (attacker);

            event.setCanceled (true);
            return;
        }

        if (!victimData.getBoolean ("PvPEnabled"))
        {
            if (attacker == event.source.getEntity ())
                disabled (attacker);

            event.setCanceled (true);
            return;
        }

        if (!attackerData.getBoolean ("PvPEnabled"))
        {
            event.setCanceled (true);
            return;
        }

        long time = PvPUtils.getTime ();

        PvPCombatLog.log (attacker.getDisplayName ()
            + " or a unit initiated an attack against "
            + victim.getDisplayName ());
    }

    /**
     * Handles PvP warmup timers.
     */
    @SubscribeEvent
    public void onLivingUpdate (LivingUpdateEvent event)
    {
        EntityPlayerMP player;
        long time = getTime ();

        if (event.entityLiving instanceof EntityPlayerMP)
            player = (EntityPlayerMP) event.entityLiving;
        else return;

        NBTTagCompound data = PvPUtils.getPvPData (player);

        long toggleTime = data.getLong ("PvPWarmup");

        if (toggleTime != 0 && toggleTime < time)
        {
            data.setLong ("PvPWarmup", 0);

            if (!data.getBoolean ("PvPEnabled"))
            {
                data.setBoolean ("PvPEnabled", true);
                warnServer (player);
            }
            else
            {
                data.setBoolean ("PvPEnabled", false);
                pvpOff (player);
            }

            data.setLong ("PvPCooldown", time + PvPMode.cooldown);
        }
    }

    /**
     * Returns the player that this entity is associated with, if possible.
     */
    public EntityPlayerMP getMaster (Entity entity)
    {
        if (entity == null)
            return null;

        if (entity instanceof EntityPlayerMP)
            return (EntityPlayerMP) entity;

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

    void fly (EntityPlayerMP player)
    {
        player.addChatMessage (new ChatComponentText (EnumChatFormatting.RED + "You are in fly mode!"));
    }

    void gm1 (EntityPlayerMP player)
    {
        player.addChatMessage (new ChatComponentText (EnumChatFormatting.RED + "You are in creative mode!"));
    }

    void disabled (EntityPlayerMP player)
    {
        player.addChatMessage (new ChatComponentText (EnumChatFormatting.RED + "This player/unit has PvP disabled!"));
    }

    void warnServer (EntityPlayerMP player)
    {
        PvPMode.cfg.sendChatMsg (new ChatComponentText (
            EnumChatFormatting.RED + "WARNING: PvP is now enabled for " + player.getDisplayName () + "!"));
    }

    void pvpOff (EntityPlayerMP player)
    {
        player.addChatComponentMessage (new ChatComponentText (
            EnumChatFormatting.GREEN + "PvP is now disabled for you."));
    }

    void sendCooldown (EntityPlayerMP player)
    {
        player.addChatMessage (new ChatComponentText (
            EnumChatFormatting.YELLOW + "You can switch PvP modes again in " + PvPMode.cooldown + " seconds."));
    }

    long getTime ()
    {
        return MinecraftServer.getSystemTimeMillis () / 1000;
    }

    public static void init ()
    {
        INSTANCE = new PvPEventHandler ();
        MinecraftForge.EVENT_BUS.register (INSTANCE);
    }

}
