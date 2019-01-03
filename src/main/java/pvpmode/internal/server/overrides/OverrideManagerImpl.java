package pvpmode.internal.server.overrides;

import java.util.*;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.*;
import net.minecraft.util.EnumChatFormatting;
import pvpmode.PvPMode;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.overrides.*;
import pvpmode.api.server.utils.*;
import pvpmode.internal.server.ServerProxy;

public class OverrideManagerImpl implements OverrideManager
{
    private final ServerProxy server;
    private final ServerConfiguration config;

    private TreeMap<Integer, Set<PvPOverrideCondition>> overrideConditions = new TreeMap<> ();

    private Map<UUID, Long> lastCheckTimes = new HashMap<> ();

    public OverrideManagerImpl ()
    {
        server = PvPMode.instance.getServerProxy ();
        config = server.getConfiguration ();
        FMLCommonHandler.instance ().bus ().register (this);
    }

    @SubscribeEvent
    public void onPlayerTick (PlayerTickEvent event)
    {
        if (!lastCheckTimes.containsKey (event.player.getUniqueID ()))
        {
            lastCheckTimes.put (event.player.getUniqueID (), 0l);
        }

        // A ton of checks whether the PvP mode of the current player can be overridden
        if (PvPServerUtils.arePvPModeOverridesEnabled () && event.phase == Phase.END
            && PvPServerUtils.getTime ()
                - lastCheckTimes.get (event.player.getUniqueID ()) >= config.getOverrideCheckInterval ()
            && !PvPServerUtils.isCreativeMode (event.player)
            && !PvPServerUtils.canFly (event.player))
        {
            PvPData pvpData = PvPServerUtils.getPvPData (event.player);

            for (Set<PvPOverrideCondition> conditions : overrideConditions.descendingMap ().values ())
            {
                for (PvPOverrideCondition condition : conditions)
                {
                    EnumForcedPvPMode newPvPMode = condition.getForcedPvPMode (event.player);
                    if (newPvPMode != EnumForcedPvPMode.UNDEFINED)
                    {
                        EnumPvPMode pvpMode = newPvPMode.toPvPMode ();
                        Boolean isPvPEnabled = pvpMode.toBoolean ();

                        if (newPvPMode != pvpData.getForcedPvPMode ()
                            && pvpMode != PvPServerUtils.getPvPMode (event.player))
                        {
                            // Only display the message if the current PvP mode really changed

                            boolean announceGlobal = isPvPEnabled ? config.isPvPEnabledAnnouncedGlobally ()
                                : config.isPvPDisabledAnnouncedGlobally ();

                            // Get the global or the local message variant
                            String message = condition.getForcedOverrideMessage (event.player, pvpMode,
                                announceGlobal);
                            if (message != null)
                            {
                                // If there's a message, post it

                                // Messages saying that PvP is enabled are red, the other ones are green
                                EnumChatFormatting messageColor = isPvPEnabled ? EnumChatFormatting.RED
                                    : EnumChatFormatting.GREEN;
                                if (announceGlobal)
                                {
                                    ServerChatUtils.postGlobalChatMessages (
                                        messageColor, message);
                                }
                                else
                                {
                                    ServerChatUtils.postLocalChatMessages (event.player, messageColor, message);
                                }
                            }
                        }
                        pvpData.setForcedPvPMode (newPvPMode);
                        pvpData.setPvPWarmup (0);// Cancel warmup timer
                        lastCheckTimes.replace (event.player.getUniqueID (), PvPServerUtils.getTime ());
                        return;// The first registered condition with the highest priority will be applied
                    }
                }

            }

            // No condition applies
            EnumForcedPvPMode currentForcedPvPMode = pvpData.getForcedPvPMode ();
            if (currentForcedPvPMode != EnumForcedPvPMode.UNDEFINED)
            {
                pvpData.setForcedPvPMode (EnumForcedPvPMode.UNDEFINED);
                if (currentForcedPvPMode.toPvPMode () != PvPServerUtils.getPvPMode (event.player))
                {
                    ServerChatUtils.green (event.player, "Your PvP mode is no longer overridden");
                }
            }

            lastCheckTimes.replace (event.player.getUniqueID (), PvPServerUtils.getTime ());
        }
    }

    @Override
    public boolean registerOverrideCondition (PvPOverrideCondition condition)
    {
        int priority = condition.getPriority ();
        if (overrideConditions.get (priority) == null)
        {
            overrideConditions.put (priority, new LinkedHashSet<> ());
        }
        return overrideConditions.get (priority).add (condition);
    }

    @Override
    public boolean unregisterOverrideCondition (PvPOverrideCondition condition)
    {
        int priority = condition.getPriority ();
        return overrideConditions.containsKey (priority) ? overrideConditions.get (priority).remove (condition) : false;
    }

}