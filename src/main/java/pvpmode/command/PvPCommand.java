package pvpmode.command;

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
        return "/pvp [cancel]";
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

        this.requireNonOverriddenSender (player);
        this.requireNonPvPSender (player);

        if (args.length > 0)
        {
            requireArgument (sender, args, 0, "cancel");

            cancelPvPTimer (player, data);
        }
        else
        {
            togglePvPMode (player, data);
        }
    }

    private void cancelPvPTimer (EntityPlayer player, PvPData data)
    {
        if (PvPUtils.isWarmupTimerRunning (player))
        {
            data.setPvPWarmup (0);
            PvPUtils.yellow (player, "PvP warmup canceled.");
        }
        else
        {
            PvPUtils.yellow (player, "No PvP warmup to cancel.");
        }
    }

    private void togglePvPMode (EntityPlayer sender, PvPData data)
    {
        if (!PvPUtils.isWarmupTimerRunning (sender))
        {
            long time = PvPUtils.getTime ();
            long toggleTime = time + PvPMode.warmup;
            long cooldownTime = data.getPvPCooldown ();

            if (cooldownTime > time)
            {
                long wait = cooldownTime - time;
                PvPUtils.yellow (sender, String.format ("Please wait %d seconds before issuing this command.", wait));
                return;
            }

            data.setPvPWarmup (toggleTime);
            data.setPvPCooldown (0);

            String status = data.isPvPEnabled () ? "disabled" : "enabled";
            PvPUtils.yellow (sender, String.format ("PvP will be %s in %d seconds...", status, PvPMode.warmup));
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
}
