package pvpmode;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class PvPUtils
{
    /**
     * Returns the system time in seconds.
     */
    public static long getTime ()
    {
        return MinecraftServer.getSystemTimeMillis () / 1000;
    }

    /**
     * Returns the EntityPlayerMP with the specified name.
     */
    public static EntityPlayerMP getPlayer (String name)
    {
        return PvPMode.cfg.func_152612_a (name);
    }

    /**
     * Determines whether the command sender has admin privileges.
     */
    public static boolean isOpped (ICommandSender sender)
    {
        return PvPMode.cfg.func_152596_g ( ((EntityPlayerMP) sender).getGameProfile ());
    }
}
