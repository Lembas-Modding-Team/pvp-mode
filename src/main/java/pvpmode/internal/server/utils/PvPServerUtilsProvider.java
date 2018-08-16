package pvpmode.internal.server.utils;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.*;
import net.minecraft.util.EnumChatFormatting;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.utils.*;
import pvpmode.internal.server.*;

public class PvPServerUtilsProvider implements PvPServerUtils.Provider
{
    private final Map<UUID, PvPData> playerData = new HashMap<> ();

    public EntityPlayerMP getPlayer (String name)
    {
        return ServerProxy.cfg.func_152612_a (name);
    }

    public boolean isOpped (ICommandSender sender)
    {
        if (sender instanceof EntityPlayerMP)
            return ServerProxy.cfg.func_152596_g ( ((EntityPlayerMP) sender).getGameProfile ());

        return true;
    }

    public PvPData getPvPData (EntityPlayer player)
    {
        if (!playerData.containsKey (player.getUniqueID ()))
        {
            playerData.put (player.getUniqueID (), new PvPDataImpl (player));
        }
        return playerData.get (player.getUniqueID ());
    }

    public int roundedDistanceBetween (EntityPlayerMP sender, EntityPlayerMP player)
    {
        double x = sender.posX - player.posX;
        double z = sender.posZ - player.posZ;

        double distance = Math.sqrt (x * x + z * z);

        return (int) (distance / ServerProxy.roundFactor + 1) * ServerProxy.roundFactor;
    }

    public boolean arePvPModeOverridesEnabled ()
    {
        return ServerProxy.overrideCheckInterval != -1;
    }

    public boolean isShiftClickingBlocked (EntityPlayer player)
    {
        return ServerProxy.blockShiftClicking && PvPServerUtils.isInPvP (player);
    }

    public void displayPvPStats (ICommandSender sender, EntityPlayer displayedPlayer)
    {
        boolean isSenderDisplayed = sender == displayedPlayer;
        PvPData data = PvPServerUtils.getPvPData (displayedPlayer);
        EnumPvPMode pvpMode = PvPServerUtils.getPvPMode (displayedPlayer);
        boolean isOverridden = data.getForcedPvPMode () != EnumForcedPvPMode.UNDEFINED;
        boolean spying = data.isSpyingEnabled ();
        long warmupTimer = PvPServerUtils.getWarmupTimer (displayedPlayer);
        long cooldownTimer = PvPServerUtils.getCooldownTimer (displayedPlayer);
        long pvpTimer = PvPServerUtils.getPvPTimer (displayedPlayer);
        boolean defaultPvPModeForced = data.isDefaultModeForced ();

        ServerChatUtils.green (sender, String.format ("------ %sPvP Stats ------", isSenderDisplayed ? "Your " : ""));
        if (!isSenderDisplayed)
        {
            ServerChatUtils.postLocalChatMessage (sender, "For: ", displayedPlayer.getDisplayName (),
                EnumChatFormatting.GRAY,
                EnumChatFormatting.DARK_GREEN);
        }
        ServerChatUtils.postLocalChatMessage (sender, "PvP Mode: ", pvpMode.toString (), EnumChatFormatting.GRAY,
            pvpMode == EnumPvPMode.ON ? EnumChatFormatting.RED : EnumChatFormatting.GREEN);
        ServerChatUtils.postLocalChatMessage (sender, "Is Overridden: ", Boolean.toString (isOverridden),
            EnumChatFormatting.GRAY, EnumChatFormatting.WHITE);
        if (ServerProxy.allowPerPlayerSpying && ServerProxy.radar)
        {
            ServerChatUtils.postLocalChatMessage (sender, "Spying Enabled: ", Boolean.toString (spying),
                EnumChatFormatting.GRAY,
                EnumChatFormatting.WHITE);
        }
        ServerChatUtils.postLocalChatMessage (sender, "Warmup Timer: ", Long.toString (warmupTimer) + "s",
            EnumChatFormatting.GRAY, warmupTimer == 0 ? EnumChatFormatting.WHITE : EnumChatFormatting.YELLOW);
        ServerChatUtils.postLocalChatMessage (sender, "Cooldown Timer: ", Long.toString (cooldownTimer) + "s",
            EnumChatFormatting.GRAY, cooldownTimer == 0 ? EnumChatFormatting.WHITE : EnumChatFormatting.YELLOW);
        ServerChatUtils.postLocalChatMessage (sender, "PvP Timer: ", Long.toString (pvpTimer) + "s",
            EnumChatFormatting.GRAY,
            pvpTimer == 0 ? EnumChatFormatting.WHITE : EnumChatFormatting.YELLOW);
        if (ServerProxy.forceDefaultPvPMode && !ServerProxy.pvpTogglingEnabled && !isOverridden)
        {
            ServerChatUtils.postLocalChatMessage (sender, "Default PvP Mode Forced: ",
                Boolean.toString (defaultPvPModeForced),
                EnumChatFormatting.GRAY,
                EnumChatFormatting.WHITE);
        }
        ServerChatUtils.green (sender, StringUtils.repeat ('-', isSenderDisplayed ? 26 : 21));
    }

}
