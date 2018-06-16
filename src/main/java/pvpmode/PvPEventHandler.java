package pvpmode;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import pvpmode.compatibility.events.EntityMasterExtractionEvent;

public class PvPEventHandler
{
    public static PvPEventHandler INSTANCE;

    /**
     * Cancels combat events associated with PvP-disabled players. Note that
     * this function will be invoked twice per attack - this is because of a
     * Forge bug.
     */
    @SubscribeEvent
    public void interceptPvP (LivingAttackEvent event)
    {
        EntityPlayerMP attacker = getMaster (event.source.getEntity ());
        EntityPlayerMP victim = getMaster (event.entity);

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

        //Via this event the compatibility modules will be asked to extract the master
        EntityMasterExtractionEvent event = new EntityMasterExtractionEvent (entity);
        return PvPUtils.postEventAndGetResult (event, event::getMaster);
    }

    public static void init ()
    {
        INSTANCE = new PvPEventHandler ();
        MinecraftForge.EVENT_BUS.register (INSTANCE);
    }

}
