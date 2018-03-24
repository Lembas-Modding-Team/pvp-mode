package pvpmode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
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

    ChatComponentText fly = new ChatComponentText (
        EnumChatFormatting.RED + "You are in fly mode!");
    ChatComponentText gm1 = new ChatComponentText (
        EnumChatFormatting.RED + "You are in creative mode!");
    ChatComponentText disabled = new ChatComponentText (
        EnumChatFormatting.RED + "This player/unit has PvP disabled!");

    /**
     * Adds a PvPEnabled tag to a new player.
     */
    @SubscribeEvent
    public void onNewPlayer (PlayerLoggedInEvent event)
    {
        EntityPlayerMP player = (EntityPlayerMP) event.player;

        if (!player.getEntityData ().hasKey ("PvPEnabled"))
        {
            player.getEntityData ().setBoolean ("PvPEnabled", false);
            player.getEntityData ().setLong ("PvPWarmup", 0);
            player.getEntityData ().setLong ("PvPCooldown", 0);
        }
    }

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

        if (attacker.capabilities.allowFlying)
        {
            attacker.addChatMessage (fly);
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
            attacker.addChatMessage (gm1);
            event.setCanceled (true);
            return;
        }

        if (!victim.getEntityData ().getBoolean ("PvPEnabled"))
        {
            attacker.addChatMessage (disabled);
            event.setCanceled (true);
            return;
        }

        if (!attacker.getEntityData ().getBoolean ("PvPEnabled"))
        {
            event.setCanceled (true);
            return;
        }
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
        long PvPWarmup = player.getEntityData ().getLong ("PvPWarmup");

        if (PvPWarmup != 0 && PvPWarmup < MinecraftServer.getSystemTimeMillis ())
        {
            // Reset the toggle time to "no warmup" state.
            player.getEntityData ().setLong ("PvPWarmup", 0);

            ServerConfigurationManager cfg = MinecraftServer.getServer ().getConfigurationManager ();

            if (!player.getEntityData ().getBoolean ("PvPEnabled"))
            {
                player.getEntityData ().setBoolean ("PvPEnabled", true);

                // Warn the whole server.
                cfg.sendChatMsg (new ChatComponentText (EnumChatFormatting.RED
                    + "PvP is now enabled for " + player.getDisplayName ()));
            }
            else
            {
                player.getEntityData ().setBoolean ("PvPEnabled", false);
                player.addChatComponentMessage (new ChatComponentText (
                    EnumChatFormatting.GREEN + "PvP is now disabled for you."));
            }

            player.getEntityData ().setLong ("PvPCooldown",
                MinecraftServer.getSystemTimeMillis () + PvPMode.cooldown);
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

    public static void init ()
    {
        INSTANCE = new PvPEventHandler ();
        MinecraftForge.EVENT_BUS.register (INSTANCE);
    }

}
