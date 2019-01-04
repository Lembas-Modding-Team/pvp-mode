package pvpmode.internal.server.command;

import java.util.*;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.*;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.*;
import pvpmode.PvPMode;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.command.ServerCommandConstants;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.utils.*;
import pvpmode.internal.server.ServerProxy;

public class PvPCommand extends AbstractPvPCommand
{

    private final ServerProxy server;
    private final ServerConfiguration config;

    public PvPCommand ()
    {
        server = PvPMode.instance.getServerProxy ();
        config = server.getConfiguration ();
    }

    @Override
    public String getCommandName ()
    {
        return ServerCommandConstants.PVP_COMMAND_NAME;
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return ServerCommandConstants.PVP_COMMAND_USAGE;
    }

    @Override
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();
        if (config.isPvPTogglingEnabled ())
        {
            messages.add (Triple.of ("pvp", "", "Starts the warmup timer to toggle PvP."));
            messages.add (Triple.of ("pvp cancel", "", "Cancels the warmup timer."));
        }
        messages.add (Triple.of ("pvp info", "", "Displays your PvP stats."));
        if (config.arePerPlayerSpyingSettingsAllowed () && config.isIntelligenceEnabled ())
        {
            messages.add (Triple.of ("pvp spy ", "[on|off]", "Toggles the spy settings."));
        }
        return messages;
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();
        if (config.isPvPTogglingEnabled ())
        {
            messages.add (
                Triple.of ("pvp", "",
                    "Starts your warmup timer. After the timer runs out, your PvP mode will be toggled. Your PvP mode mustn't be overridden."));
            messages.add (Triple.of ("pvp cancel", "",
                "Cancels your warmup timer, if it's running. Your PvP mode mustn't be overridden."));
        }
        messages.add (Triple.of ("pvp info", "",
            "Displays your PvP mode, your spying settings, your warmup, cooldown and PvP timer, whether your PvP mode is overridden, and other PvP Mode Mod related stats about you."));
        if (config.arePerPlayerSpyingSettingsAllowed () && config.isIntelligenceEnabled ())
        {
            messages.add (Triple.of ("pvp spy ", "[on|off]",
                "Allows you to either toggle your spy settings, or to set them to a specific value (on or off). If spying is enabled for you, you can receive proximity and direction information about other players with PvP enabled (via the PvP list). PvP needs to be enabled for you."));
        }
        return messages;
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "Allows you to manage and view your PvP mode (OFF, ON without spy, ON with spy). You mustn't be able to fly, not in creative mode and not in PvP combat if you want to modify your PvP stats with this command.";
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args, String[] originalArgs)
    {
        EntityPlayerMP player = getCommandSenderAsPlayer (sender);
        PvPData data = PvPServerUtils.getPvPData (player);

        if (args.length > 0)
        {
            switch (requireArguments (sender, args, 0, "cancel", "spy", "info"))
            {
                case "cancel":
                    requireToggelingEnabled ();
                    requireNonFlyingNonCreativeNonCombatSender (player);
                    requireNonOverriddenSender (player);
                    cancelPvPTimer (player, data);
                    break;
                case "spy":
                    requireSpying ();
                    requireNonFlyingNonCreativeNonCombatSender (player);
                    requireSenderWithPvPEnabled (player);
                    if (args.length > 1)
                    {
                        if (requireArguments (sender, args, 1, "on", "off").equals ("on"))
                        {
                            toggleSpyMode (player, data, Boolean.TRUE);
                        }
                        else
                        {
                            toggleSpyMode (player, data, Boolean.FALSE);
                        }
                    }
                    else
                    {
                        toggleSpyMode (player, data, null);
                    }
                    break;
                case "info":
                    PvPServerUtils.displayPvPStats (player, player);
                    break;
            }
        }
        else

        {
            requireToggelingEnabled ();
            requireNonFlyingNonCreativeNonCombatSender (player);
            requireNonOverriddenSender (player);
            togglePvPMode (player, data);
        }
    }

    private void requireSpying ()
    {
        if (! (config.arePerPlayerSpyingSettingsAllowed () && config.isIntelligenceEnabled ()))
        {
            featureDisabled ();
        }
    }

    private void requireToggelingEnabled ()
    {
        if (!config.isPvPTogglingEnabled ())
        {
            featureDisabled ();
        }
    }

    private void requireNonFlyingNonCreativeNonCombatSender (EntityPlayer player)
    {
        requireNonCreativeSender (player);
        requireNonFlyingSender (player);
        requireNonPvPSender (player);
    }

    private void cancelPvPTimer (EntityPlayer player, PvPData data)
    {
        if (PvPServerUtils.isWarmupTimerRunning (player))
        {
            data.setPvPWarmup (0);
            ServerChatUtils.green (player, "The warmup timer was canceled");
        }
        else
        {
            ServerChatUtils.yellow (player, "The warmup timer isn't running");
        }
    }

    private void togglePvPMode (EntityPlayer sender, PvPData data)
    {
        if (!PvPServerUtils.isWarmupTimerRunning (sender))
        {
            long time = PvPServerUtils.getTime ();
            long warmup = data.isPvPEnabled () ? config.getWarmupOnOff () : config.getWarmupOffOn ();
            long toggleTime = time + warmup;
            long cooldownTime = data.getPvPCooldown ();

            if (cooldownTime > time)
            {
                long wait = cooldownTime - time;
                ServerChatUtils.red (sender,
                    String.format ("Please wait %d seconds before issuing this command", wait));
                return;
            }

            data.setPvPWarmup (toggleTime);
            data.setPvPCooldown (0);

            ServerChatUtils.green (sender, String.format ("PvP will be %s in %d seconds...",
                PvPCommonUtils.getEnabledString (!data.isPvPEnabled ()), warmup));
        }
        else
        {
            ChatComponentText firstPart = new ChatComponentText ("The warmup timer is already running. Use ");
            ChatComponentText secondPart = new ChatComponentText ("/pvp cancel");
            ChatComponentText thirdPart = new ChatComponentText (" to cancel it");

            firstPart.getChatStyle ().setColor (EnumChatFormatting.RED);
            secondPart.getChatStyle ().setChatClickEvent (new ClickEvent (Action.SUGGEST_COMMAND, "/pvp cancel"))
                .setColor (EnumChatFormatting.DARK_RED);
            thirdPart.getChatStyle ().setColor (EnumChatFormatting.RED);
            sender.addChatMessage (firstPart.appendSibling (secondPart).appendSibling (thirdPart));
        }
    }

    private void toggleSpyMode (EntityPlayer sender, PvPData data, Boolean mode)
    {

        if (mode == null ? true : mode.booleanValue () != data.isSpyingEnabled ())
        {
            data.setSpyingEnabled (!data.isSpyingEnabled ());
            ServerChatUtils.green (sender,
                String.format ("Spying is now %s for you", PvPCommonUtils.getEnabledString (data.isSpyingEnabled ())));
        }
        else
        {
            ServerChatUtils.yellow (sender,
                String.format ("Spying is already %s for you", PvPCommonUtils.getEnabledString (mode.booleanValue ())));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getTabCompletionOptions (ICommandSender sender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord (args, "info", "cancel", "spy");
        if (args.length == 2 && args[0].equals ("spy"))
            return getListOfStringsMatchingLastWord (args, "on", "off");
        return null;
    }
}
