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
        // Safe players are listed in green, PvP-enabled players in red.
        ServerConfigurationManager cfg = MinecraftServer.getServer ().getConfigurationManager ();
        StringBuilder display = new StringBuilder ();

        for (Object o : cfg.playerEntityList)
        {
            EntityPlayerMP player = (EntityPlayerMP) o;

            display.append (player.getDisplayName () + ": ");

            if (player.getEntityData ().getBoolean ("PvPDenied"))
                display.append (EnumChatFormatting.GREEN + "OFF\n");
            else
            {
                EntityPlayerMP playerSender = getCommandSenderAsPlayer (sender);

                // This is a very fuzzy distance metric.
                // It only shows player distances to the nearest 16 chunks or
                // so, and even that not very reliably.
                int deltaX = playerSender.chunkCoordX - player.chunkCoordX;
                int deltaZ = playerSender.chunkCoordZ - player.chunkCoordZ;

                int distance = (int) (Math.sqrt (deltaX * deltaX + deltaZ * deltaZ) + 1);
                distance = (distance / 16 + 1) * 16;

                display.append (EnumChatFormatting.RED + "ON - ~" + distance + " chunks distant\n");
            }
        }

        ChatComponentText message = new ChatComponentText (display.toString ());
        sender.addChatMessage (message);
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }
}
