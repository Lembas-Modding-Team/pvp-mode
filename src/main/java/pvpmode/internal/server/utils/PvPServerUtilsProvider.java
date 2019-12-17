package pvpmode.internal.server.utils;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.*;
import net.minecraft.util.EnumChatFormatting;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.utils.*;
import pvpmode.internal.server.*;

public class PvPServerUtilsProvider implements PvPServerUtils.Provider
{

    private final ServerProxy server;
    private ServerConfiguration config;

    private final Map<UUID, PvPData> playerData = new HashMap<> ();

    public PvPServerUtilsProvider (ServerProxy server)
    {
        this.server = server;
    }

    public void preInit ()
    {
        this.config = server.getConfiguration ();
    }

    @Override
    public EntityPlayerMP getPlayer (String name)
    {
        return server.getServerConfigurationManager ().func_152612_a (name);
    }

    @Override
    public boolean isOpped (ICommandSender sender)
    {
        if (sender instanceof EntityPlayerMP)
            return server.getServerConfigurationManager ()
                .func_152596_g ( ((EntityPlayerMP) sender).getGameProfile ());

        return true;
    }

    @Override
    public PvPData getPvPData (EntityPlayer player)
    {
        if (!playerData.containsKey (player.getUniqueID ()))
        {
            playerData.put (player.getUniqueID (), new PvPDataImpl (player));
        }
        return playerData.get (player.getUniqueID ());
    }

    @Override
    public int roundedDistanceBetween (EntityPlayerMP sender, EntityPlayerMP player)
    {
        double x = sender.posX - player.posX;
        double z = sender.posZ - player.posZ;

        double distance = Math.sqrt (x * x + z * z);

        return (int) (distance / config.getDistanceRoundingFactor () + 1) * config.getDistanceRoundingFactor ();
    }

    @Override
    public boolean arePvPModeOverridesEnabled ()
    {
        return config.getOverrideCheckInterval () != -1;
    }

    @Override
    public boolean isShiftClickingBlocked (EntityPlayer player)
    {
        return config.isFastItemTransferDisabled () && PvPServerUtils.isInPvP (player);
    }

    @Override
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

        EnumChatFormatting prefixColor = EnumChatFormatting.WHITE;
        EnumChatFormatting valueColor = EnumChatFormatting.YELLOW;

        ServerChatUtils.green (sender, String.format ("------ %sPvP Stats ------", isSenderDisplayed ? "Your " : ""));
        if (!isSenderDisplayed)
        {
            ServerChatUtils.postLocalChatMessage (sender, "For: ", displayedPlayer.getDisplayName (),
                prefixColor,
                EnumChatFormatting.DARK_GREEN);
        }
        ServerChatUtils.postLocalChatMessage (sender, "PvP Mode: ", pvpMode.toString (), prefixColor,
            pvpMode == EnumPvPMode.ON ? EnumChatFormatting.RED : EnumChatFormatting.GREEN);
        ServerChatUtils.postLocalChatMessage (sender, "Is Overridden: ", Boolean.toString (isOverridden),
            prefixColor, valueColor);
        if (config.arePerPlayerSpyingSettingsAllowed () && config.isIntelligenceEnabled ())
        {
            ServerChatUtils.postLocalChatMessage (sender, "Spying Enabled: ", Boolean.toString (spying),
                prefixColor,
                valueColor);
        }
        ServerChatUtils.postLocalChatMessage (sender, "Warmup Timer: ", Long.toString (warmupTimer) + "s",
            prefixColor, warmupTimer == 0 ? valueColor : EnumChatFormatting.GOLD);
        ServerChatUtils.postLocalChatMessage (sender, "Cooldown Timer: ", Long.toString (cooldownTimer) + "s",
            prefixColor, cooldownTimer == 0 ? valueColor : EnumChatFormatting.GOLD);
        ServerChatUtils.postLocalChatMessage (sender, "PvP Timer: ", Long.toString (pvpTimer) + "s",
            prefixColor,
            pvpTimer == 0 ? valueColor : EnumChatFormatting.GOLD);
        if (config.isDefaultPvPModeForced () && !config.isPvPTogglingEnabled () && !isOverridden)
        {
            ServerChatUtils.postLocalChatMessage (sender, "Default PvP Mode Forced: ",
                Boolean.toString (defaultPvPModeForced),
                prefixColor,
                valueColor);
        }
        ServerChatUtils.green (sender, StringUtils.repeat ('-', isSenderDisplayed ? 26 : 21));
    }

    @Override
    public boolean isInPvP (EntityPlayer player)
    {
        return PvPServerUtils.getPvPData (player).getPvPTimer () != 0;
    }

}
