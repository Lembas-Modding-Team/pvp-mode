package pvpmode.overrides;

import java.util.*;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.*;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.util.EnumChatFormatting;
import pvpmode.*;

/**
 * The override manager manages the conditional PvP mode overrides. Custom
 * override conditions can be registered here.
 * 
 * @author CraftedMods
 *
 */
public class PvPOverrideManager
{
    private TreeMap<Integer, Set<PvPOverrideCondition>> overrideConditions = new TreeMap<> ();

    private Map<UUID, Long> lastCheckTimes = new HashMap<> ();

    /**
     * Registers a new PvP override condition.<br/>
     * Override conditions can be registered everytime.
     * 
     * @param condition
     *            The condition to register
     * @return Whether the condition could be registered
     */
    public boolean registerOverrideCondition (PvPOverrideCondition condition)
    {
        int priority = condition.getPriority ();
        if (overrideConditions.get (priority) == null)
            overrideConditions.put (priority, new LinkedHashSet<> ());
        return overrideConditions.get (priority).add (condition);
    }

    /**
     * Unregisters a new PvP override condition.<br/>
     * Override conditions can be unregistered everytime.
     * 
     * @param condition
     *            The condition to unregister
     * @return Whether the condition could be unregistered
     */
    public boolean unregisterOverrideCondition (PvPOverrideCondition condition)
    {
        int priority = condition.getPriority ();
        return overrideConditions.containsKey (priority) ? overrideConditions.get (priority).remove (condition) : false;
    }

    public PvPOverrideManager ()
    {
        FMLCommonHandler.instance ().bus ().register (this);
    }

    @SubscribeEvent
    public void onPlayerTick (PlayerTickEvent event)
    {
        if (!lastCheckTimes.containsKey (event.player.getUniqueID ()))
            lastCheckTimes.put (event.player.getUniqueID (), 0l);

        // A ton of checks whether the PvP mode of the current player can be
        // overridden
        if (PvPUtils.arePvPModeOverridesEnabled () && event.side == Side.SERVER && event.phase == Phase.END
            && (PvPUtils.getTime () - lastCheckTimes.get (event.player.getUniqueID ())) >= PvPMode.overrideCheckInterval
            && !PvPUtils.isCreativeMode (event.player)
            && !PvPUtils.canFly (event.player))
        {
            PvPData pvpData = PvPUtils.getPvPData (event.player);

            for (Set<PvPOverrideCondition> conditions : overrideConditions.descendingMap ().values ())
            {
                for (PvPOverrideCondition condition : conditions)
                {
                    Boolean isPvPEnabled = condition.isPvPEnabled (event.player);
                    if (isPvPEnabled != null)
                    {
                        EnumForcedPvPMode newPvPMode = isPvPEnabled ? EnumForcedPvPMode.ON : EnumForcedPvPMode.OFF;
                        if (newPvPMode != pvpData.getForcedPvPMode ()
                            && newPvPMode.toPvPMode () != PvPUtils.getPvPMode (event.player))
                        {
                            // Only display the message if the current PvP mode
                            // really changed
                            String message = condition.getForcedOverrideMessage (event.player, isPvPEnabled);
                            if (message != null)
                            {
                                ChatUtils.postGlobalChatMessages (EnumChatFormatting.RED, message);
                            }
                        }
                        pvpData.setForcedPvPMode (newPvPMode);
                        pvpData.setPvPWarmup (0);// Cancel warmup timer
                        lastCheckTimes.replace (event.player.getUniqueID (), PvPUtils.getTime ());
                        return;// The first registered condition with the
                               // highest priority will be applied
                    }
                }

            }

            // No condition applies
            EnumForcedPvPMode currentForcedPvPMode = pvpData.getForcedPvPMode ();
            if (currentForcedPvPMode != EnumForcedPvPMode.UNDEFINED)
            {
                pvpData.setForcedPvPMode (EnumForcedPvPMode.UNDEFINED);
                if (currentForcedPvPMode.toPvPMode () != PvPUtils.getPvPMode (event.player))
                {
                    ChatUtils.green (event.player, "Your PvP mode is no longer overridden");
                }
            }

            lastCheckTimes.replace (event.player.getUniqueID (), PvPUtils.getTime ());
        }
    }
}
