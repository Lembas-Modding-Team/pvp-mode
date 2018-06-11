package pvpmode;

import java.lang.reflect.*;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PvPEventHandler
{
    public static PvPEventHandler INSTANCE;

    /**
     * True if data from hired units of the LOTR Mod couldn't be accessed. This
     * prevents PvPMode trying to access them again and again if it failed
     * before.
     */
    private boolean lotrPatchFailed = false;

    /**
     * Cancels combat events associated with PvP-disabled players. Note that
     * this function will be invoked twice per attack - this is because of a
     * Forge bug.
     */
    @SubscribeEvent
    public void interceptPvP (LivingAttackEvent event)
    {
        EntityPlayerMP attacker = this.getMaster (event.source.getEntity ());
        EntityPlayerMP victim = this.getMaster (event.entity);

        if (attacker == null || victim == null)
            return;

        EnumPvPMode attackerMode = PvPUtils.getPvPMode (attacker);
        EnumPvPMode victimMode = PvPUtils.getPvPMode (victim);

        boolean cancel = false;

        if (attackerMode != EnumPvPMode.ON)
        {
            if (attacker == event.source.getEntity ())
            {
                if (PvPUtils.isCreativeMode (attacker))
                {
                    PvPUtils.red (attacker, "You are in creative mode!");
                }
                else if (PvPUtils.canFly (attacker))
                {
                    PvPUtils.red (attacker, "You are in fly mode!");
                }
            }
            cancel = true;
        }

        if (cancel) {// For performance reasons
            event.setCanceled (true);
            return;
        }

        if (victimMode != EnumPvPMode.ON)
        {
            if (attacker == event.source.getEntity ())
            {
                PvPUtils.red (attacker, "This player/unit has PvP disabled!");
            }
            cancel = true;
        }

        if (cancel)
            event.setCanceled (true);
    }

    /*
     * We need to log here because the LivingAttackEvent will be fired twice per
     * attack.
     */
    @SubscribeEvent
    public void onLivingHurt (LivingHurtEvent event)
    {
        EntityPlayerMP attacker = getMaster (event.source.getEntity ());
        EntityPlayerMP victim = getMaster (event.entity);

        if (attacker == null || victim == null)
            return;

        if (PvPMode.activatedPvPLoggingHandlers.size () > 0)
            PvPMode.combatLogManager.log (attacker, victim, event.ammount, event.source);
    }

    /**
     * Handles PvP warmup timers.
     */
    @SubscribeEvent
    public void onLivingUpdate (LivingUpdateEvent event)
    {
        EntityPlayerMP player;
        long time = PvPUtils.getTime ();

        if (event.entityLiving instanceof EntityPlayerMP)
            player = (EntityPlayerMP) event.entityLiving;
        else return;

        PvPData data = PvPUtils.getPvPData (player);

        long toggleTime = data.getPvPWarmup ();

        if (toggleTime != 0 && toggleTime < time)
        {
            data.setPvPWarmup (0);

            if (!data.isPvPEnabled ())
            {
                data.setPvPEnabled (true);
                PvPMode.cfg.sendChatMsg (new ChatComponentText (
                    EnumChatFormatting.RED + "WARNING: PvP is now enabled for " + player.getDisplayName () + "!"));
            }
            else
            {
                data.setPvPEnabled (false);
                PvPUtils.green (player, "PvP is now disabled for you.");
            }

            data.setPvPCooldown (time + PvPMode.cooldown);
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

        if (entity instanceof IEntityOwnable)
            return (EntityPlayerMP) ((IEntityOwnable) entity).getOwner ();

        if (PvPUtils.isLOTRModLoaded () && !lotrPatchFailed)
        {
            // LOTR patch begins.

            Class<?> entityClass = entity.getClass ();
            try
            {
                if (Class.forName ("lotr.common.entity.npc.LOTREntityNPC").isAssignableFrom (entityClass))
                {
                    // Only Gandalf, Dumbledore, Mahtaran and I are capable of
                    // this kind
                    // of magic.
                    // And even then it requires a silken hand and a subtle
                    // touch.
                    Field hiredUnitInfo = entityClass.getField ("hiredNPCInfo");
                    Class<?> hiredClass = hiredUnitInfo.getType ();
                    Method getHiringPlayer = hiredClass.getMethod ("getHiringPlayer");

                    Object o = getHiringPlayer.invoke (hiredUnitInfo.get (entity));

                    if (o instanceof EntityPlayerMP)
                        return (EntityPlayerMP) o;
                }
                else {
                    // This entity is not a LOTR unit.
                    return null;
                }
            }
            catch (ClassNotFoundException ex)
            {
                // This shouldn't be able to happen
                FMLLog.getLogger ().error (
                    "Some required classes of the LOTR Mod couldn't be found, it looks like the internal code of the LOTR Mod changed",
                    ex);
                lotrPatchFailed = true;
                return null;
            }
            catch (NoSuchFieldException ex)
            {
                // Something changed with the LOTR mod.
                FMLLog.getLogger ().error (
                    "Some required fields of the LOTR Mod couldn't be found, it looks like the internal code of the LOTRMod changed",
                    ex);
                lotrPatchFailed = true;
                return null;
            }
            catch (NoSuchMethodException ex)
            {
                // Something changed with the LOTR mod.
                FMLLog.getLogger ().error (
                    "Some required methods of the LOTR Mod couldn't be found, it looks like the internal code of the LOTRMod changed",
                    ex);
                lotrPatchFailed = true;
                return null;
            }
            catch (IllegalAccessException ex)
            {
                // Hopefully impossible since I am only accessing public
                // fields/methods.
                FMLLog.getLogger ().error ("Security exception in PvPEventHandler at " + entityClass, ex);
                lotrPatchFailed = true;
                return null;
            }
            catch (InvocationTargetException ex)
            {
                // If the invoked method threw an exception it'll be wrapped in
                // an InvocationTargetException
                FMLLog.getLogger ().error ("A function of the LOTR Mod trew an exception", ex);
                lotrPatchFailed = true;
                return null;
            }
        }

        return null;
    }

    public static void init ()
    {
        INSTANCE = new PvPEventHandler ();
        MinecraftForge.EVENT_BUS.register (INSTANCE);
    }

}
