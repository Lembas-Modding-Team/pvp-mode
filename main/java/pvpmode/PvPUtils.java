package pvpmode;

import cpw.mods.fml.common.Loader;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class PvPUtils
{
    private static Boolean lotrmodLoaded;

    /**
     * Returns whether or not the LOTRMod is loaded
     */
    public static boolean isLOTRModLoaded()
    {
        if (lotrmodLoaded == null)
            lotrmodLoaded = Loader.isModLoaded ("lotr");
        return lotrmodLoaded.booleanValue ();
    }

    /**
     * Returns the system time in seconds.
     */
    public static long getTime()
    {
        return MinecraftServer.getSystemTimeMillis () / 1000;
    }

    /**
     * Returns the EntityPlayerMP with the specified name.
     */
    public static EntityPlayerMP getPlayer(String name)
    {
        return PvPMode.cfg.func_152612_a (name);
    }

    /**
     * Determines whether the command sender has admin privileges.
     */
    public static boolean isOpped(ICommandSender sender)
    {
        if (sender instanceof EntityPlayerMP)
            return PvPMode.cfg.func_152596_g ( ((EntityPlayerMP) sender).getGameProfile ());

        return true;
    }

    /**
     * Returns the data tag from which all player-specific PvP properties can be
     * accessed.
     */
    public static NBTTagCompound getPvPData(EntityPlayer player)
    {
        NBTTagCompound data = player.getEntityData ();
        NBTTagCompound persistent;
        NBTTagCompound pvpData;

        if (!data.hasKey (EntityPlayer.PERSISTED_NBT_TAG))
        {
            persistent = new NBTTagCompound ();
            data.setTag (EntityPlayer.PERSISTED_NBT_TAG, persistent);
        }

        persistent = data.getCompoundTag (EntityPlayer.PERSISTED_NBT_TAG);

        if (!persistent.hasKey ("PvPData"))
        {
            pvpData = new NBTTagCompound ();
            persistent.setTag ("PvPData", pvpData);
        }

        pvpData = persistent.getCompoundTag ("PvPData");

        if (!pvpData.hasKey ("PvPEnabled"))
            pvpData.setBoolean ("PvPEnabled", false);

        if (!pvpData.hasKey ("PvPWarmup"))
            pvpData.setLong ("PvPWarmup", 0);

        if (!pvpData.hasKey ("PvPCooldown"))
            pvpData.setLong ("PvPCooldown", 0);

        if (!pvpData.hasKey ("PvPTag"))
            pvpData.setLong ("PvPTag", 0);

        return pvpData;
    }

    /**
     * Displays the specified message to the recipient in yellow.
     */
    public static void yellow(ICommandSender recipient, String message)
    {
        message = EnumChatFormatting.YELLOW + message;
        ChatComponentText text = new ChatComponentText (message);
        recipient.addChatMessage (text);
    }

    /**
     * Displays the specified message to the recipient in red.
     */
    public static void red(ICommandSender recipient, String message)
    {
        message = EnumChatFormatting.RED + message;
        ChatComponentText text = new ChatComponentText (message);
        recipient.addChatMessage (text);
    }

    /**
     * Displays the specified message to the recipient in green.
     */
    public static void green(ICommandSender recipient, String message)
    {
        message = EnumChatFormatting.GREEN + message;
        ChatComponentText text = new ChatComponentText (message);
        recipient.addChatMessage (text);
    }

    /**
     * Displays the specified message to the recipient in white.
     */
    public static void white(ICommandSender recipient, String message)
    {
        ChatComponentText text = new ChatComponentText (message);
        recipient.addChatMessage (text);
    }
}
