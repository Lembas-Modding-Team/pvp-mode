package pvpmode.command;

import java.util.ArrayList;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import pvpmode.*;

public class PvPCommandList extends CommandBase
{
    @Override
    public String getCommandName ()
    {
        return "pvplist";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return "/pvplist";
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        EntityPlayerMP senderPlayer = getCommandSenderAsPlayer (sender);
        ArrayList<String> safePlayers = new ArrayList<String> ();
        ArrayList<String> unsafePlayers = new ArrayList<String> ();

        for (Object o : PvPMode.cfg.playerEntityList)
        {
            EntityPlayerMP player = (EntityPlayerMP) o;
            PvPData playerData = PvPUtils.getPvPData (player);

            if (player.capabilities.isCreativeMode)
                safePlayers.add ("[GM1] " + player.getDisplayName ());
            else if (player.capabilities.allowFlying)
                safePlayers.add ("[FLY] " + player.getDisplayName ());
            else if (!playerData.isPvPEnabled ())
            {
                long warmup = playerData.getPvPWarmup ();

                if (warmup == 0)
                    safePlayers.add ("[OFF] " + player.getDisplayName ());
                else
                    unsafePlayers.add (EnumChatFormatting.YELLOW + "[WARMUP] " + player.getDisplayName ()
                        + " - " + (warmup - PvPUtils.getTime ()) + " seconds till PvP");
            }
            else
            {
                String message = EnumChatFormatting.RED + "[ON] " + player.getDisplayName ();

                if (PvPUtils.getPvPData (senderPlayer).isPvPEnabled () && PvPMode.radar
                    && senderPlayer != player)
                    message += " - ~" + roundedDistanceBetween (senderPlayer, player) + " blocks";

                unsafePlayers.add (message);
            }
        }

        for (String line : unsafePlayers)
            sender.addChatMessage (new ChatComponentText (line));
        for (String line : safePlayers)
            PvPUtils.green (sender, line);
    }

    int roundedDistanceBetween (EntityPlayerMP sender, EntityPlayerMP player)
    {
        double x = sender.posX - player.posX;
        double z = sender.posZ - player.posZ;

        double distance = Math.sqrt (x * x + z * z);

        return (int) ( (distance) / PvPMode.roundFactor + 1) * PvPMode.roundFactor;
    }
}
