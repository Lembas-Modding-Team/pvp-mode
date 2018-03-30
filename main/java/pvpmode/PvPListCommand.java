package pvpmode;

import java.util.ArrayList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class PvPListCommand extends CommandBase
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
    public void processCommand (ICommandSender sender, String[] args)
    {
        EntityPlayerMP senderPlayer = getCommandSenderAsPlayer (sender);
        ArrayList<String> safePlayers = new ArrayList<String> ();
        ArrayList<String> unsafePlayers = new ArrayList<String> ();

        for (Object o : PvPMode.cfg.playerEntityList)
        {
            EntityPlayerMP player = (EntityPlayerMP) o;

            if (player.getDisplayName ().equals (senderPlayer.getDisplayName ()))
                continue;

            if (player.capabilities.isCreativeMode)
                safePlayers.add (EnumChatFormatting.GREEN + "[GM1] " + player.getDisplayName ());
            else if (player.capabilities.allowFlying)
                safePlayers.add (EnumChatFormatting.GREEN + "[FLY] " + player.getDisplayName ());
            else if (!player.getEntityData ().getBoolean ("PvPEnabled"))
            {
                long warmup = PvPUtils.getTime () - player.getEntityData ().getLong ("PvPWarmup");

                if (warmup == 0)
                    safePlayers.add (EnumChatFormatting.GREEN + "[OFF] " + player.getDisplayName ());
                else
                    unsafePlayers.add (EnumChatFormatting.YELLOW + "[WARMUP] " + player.getDisplayName ()
                        + " - " + warmup + " seconds till PvP");
            }
            else
            {
                String message = EnumChatFormatting.RED + "[ON] " + player.getDisplayName ();

                if (senderPlayer.getEntityData ().getBoolean ("PvPEnabled") && PvPMode.radar)
                    message += " - ~" + roundedDistanceBetween (senderPlayer, player) + " blocks";

                unsafePlayers.add (message);
            }
        }

        for (String line : unsafePlayers)
            sender.addChatMessage (new ChatComponentText (line));
        for (String line : safePlayers)
            sender.addChatMessage (new ChatComponentText (line));
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    int roundedDistanceBetween (EntityPlayerMP sender, EntityPlayerMP player)
    {
        double x = sender.posX - player.posX;
        double z = sender.posZ - player.posZ;

        double distance = Math.sqrt (x * x + z * z);

        return (int) (distance) / PvPMode.roundFactor * PvPMode.roundFactor;
    }
}
