package pvpmode.command;

import java.util.*;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.*;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.*;
import pvpmode.*;

public class PvPCommand extends AbstractPvPCommand
{
    @Override
    public String getCommandName ()
    {
        return "pvp";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return "/pvp [cancel] OR /pvp spy [on|off]";
    }

    @Override
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();
        messages.add (Triple.of ("pvp", "", "Starts the warmup timer to toggle PvP."));
        messages.add (Triple.of ("pvp cancel", "", "Cancels the warmup timer."));
        if (PvPMode.allowPerPlayerSpying && PvPMode.radar)
        {
            messages.add (Triple.of ("pvp spy ", "[on|off]", "Toggles the spy settings."));
        }
        return messages;
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();
        messages.add (
            Triple.of ("pvp", "",
                "Starts your warmup timer. After the timer runs out, your PvP mode will be toggled. Your PvP mode mustn't be overridden."));
        messages.add (Triple.of ("pvp cancel", "",
            "Cancels your warmup timer, if it's running. Your PvP mode mustn't be overridden."));
        if (PvPMode.allowPerPlayerSpying && PvPMode.radar)
        {
            messages.add (Triple.of ("pvp spy ", "[on|off]",
                "Allows you to either toggle your spy settings, or to set them to a specific value (on or off). If spying is enabled for you, you can receive proximity and direction information about other players with PvP enabled (via the PvP list). PvP needs to be enabled for you."));
        }
        return messages;
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "Allows you to manage your PvP mode (OFF, ON without spy, ON with spy). You mustn't be able to fly, not in creative mode and not in PvP combat to use this command.";
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        EntityPlayerMP player = getCommandSenderAsPlayer (sender);
        PvPData data = PvPUtils.getPvPData (player);

        this.requireNonCreativeSender (player);
        this.requireNonFlyingSender (player);
        this.requireNonPvPSender (player);

        if (args.length > 0)
        {
            switch (this.requireArguments (sender, args, 0, "cancel", "spy"))
            {
                case "cancel":
                    this.requireNonOverriddenSender (player);
                    cancelPvPTimer (player, data);
                    break;
                case "spy":
                    if (PvPMode.allowPerPlayerSpying && PvPMode.radar)
                    {
                        this.requireSenderWithPvPEnabled (player);
                        if (args.length > 1)
                        {
                            if (this.requireArguments (sender, args, 1, "on", "off").equals ("on"))
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
                    }
                    else
                    {
                        ChatUtils.red (player, "This feature was disabled by the server");
                    }
                    break;
            }
        }
        else
        {
            this.requireNonOverriddenSender (player);
            togglePvPMode (player, data);
        }
    }

    private void cancelPvPTimer (EntityPlayer player, PvPData data)
    {
        if (PvPUtils.isWarmupTimerRunning (player))
        {
            data.setPvPWarmup (0);
            ChatUtils.green (player, "The warmup timer was canceled");
        }
        else
        {
            ChatUtils.yellow (player, "The warmup timer isn't running");
        }
    }

    private void togglePvPMode (EntityPlayer sender, PvPData data)
    {
        if (!PvPUtils.isWarmupTimerRunning (sender))
        {
            long time = PvPUtils.getTime ();
            long warmup = data.isPvPEnabled () ? PvPMode.warmupOff : PvPMode.warmup;
            long toggleTime = time + warmup;
            long cooldownTime = data.getPvPCooldown ();

            if (cooldownTime > time)
            {
                long wait = cooldownTime - time;
                ChatUtils.red (sender, String.format ("Please wait %d seconds before issuing this command", wait));
                return;
            }

            data.setPvPWarmup (toggleTime);
            data.setPvPCooldown (0);

            ChatUtils.green (sender, String.format ("PvP will be %s in %d seconds...",
                PvPUtils.getEnabledString (!data.isPvPEnabled ()), warmup));
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
            ChatUtils.green (sender,
                String.format ("Spying is now %s for you", PvPUtils.getEnabledString (data.isSpyingEnabled ())));
        }
        else
        {
            ChatUtils.yellow (sender,
                String.format ("Spying is already %s for you", PvPUtils.getEnabledString (mode.booleanValue ())));
        }
    }
}
