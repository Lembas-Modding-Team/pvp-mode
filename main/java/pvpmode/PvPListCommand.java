package pvpmode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
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
        ServerConfigurationManager cfg = MinecraftServer.getServer ().getConfigurationManager ();
        EntityPlayerMP senderPlayer = getCommandSenderAsPlayer (sender);
        StringBuilder safePlayers = new StringBuilder ();
        StringBuilder unsafePlayers = new StringBuilder ();

        for (Object o : cfg.playerEntityList)
        {
            EntityPlayerMP player = (EntityPlayerMP) o;

            if (player.capabilities.allowFlying)
                safePlayers.append (EnumChatFormatting.GREEN + "[FLY] " + player.getDisplayName () + "\n");
            else if (player.capabilities.isCreativeMode)
                safePlayers.append (EnumChatFormatting.GREEN + "[GM1] " + player.getDisplayName () + "\n");
            else if (!player.getEntityData ().getBoolean ("PvPEnabled"))
                safePlayers.append (EnumChatFormatting.GREEN + "[OFF] " + player.getDisplayName () + "\n");
            else
                unsafePlayers.append (EnumChatFormatting.RED + "[ON] " + player.getDisplayName ()
                    + " - ~" + roundedDistanceBetween (senderPlayer, player) + " blocks" + "\n");
        }

        sender.addChatMessage (new ChatComponentText (unsafePlayers.toString () + "\n"));
        sender.addChatMessage (new ChatComponentText (safePlayers.toString () + "\n"));
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    public int roundedDistanceBetween (EntityPlayerMP sender, EntityPlayerMP player)
    {
        double x = sender.posX - player.posX;
        double z = sender.posZ - player.posZ;

        double distance = Math.sqrt (x * x + z * z);

        return (int) (distance) / 64 * 64;
    }
}
