package pvpmode.internal.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import pvpmode.PvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.api.server.PvPData;

public class PvPDataImpl implements PvPData
{
    private NBTTagCompound pvpDataTag;

    private static final String PVP_DATA_NBT_KEY = "PvPData";

    private static final String PVP_ENABLED_NBT_KEY = "PvPEnabled";
    private static final String PVP_WARMUP_NBT_KEY = "PvPWarmup";
    private static final String PVP_COOLDOWN_NBT_KEY = "PvPCooldown";
    private static final String FORCED_PVP_MODE_NBT_KEY = "ForcedPvPMode";
    private static final String PVP_TIMER_NBT_KEY = "PvPTimer";
    private static final String SPYING_ENABLED_NBT_KEY = "Spying";
    private static final String DEFAULT_MODE_FORCED_NBT_KEY = "DefaultModeForced";

    public PvPDataImpl (EntityPlayer player)
    {
        NBTTagCompound data = player.getEntityData ();
        NBTTagCompound persistent;

        if (!data.hasKey (EntityPlayer.PERSISTED_NBT_TAG))
        {
            persistent = new NBTTagCompound ();
            data.setTag (EntityPlayer.PERSISTED_NBT_TAG, persistent);
        }

        persistent = data.getCompoundTag (EntityPlayer.PERSISTED_NBT_TAG);

        if (!persistent.hasKey (PVP_DATA_NBT_KEY))
        {
            pvpDataTag = new NBTTagCompound ();
            persistent.setTag (PVP_DATA_NBT_KEY, pvpDataTag);
        }

        pvpDataTag = persistent.getCompoundTag (PVP_DATA_NBT_KEY);
    }

    @Override
    public boolean isPvPEnabled ()
    {
        return pvpDataTag.hasKey (PVP_ENABLED_NBT_KEY) ? pvpDataTag.getBoolean (PVP_ENABLED_NBT_KEY)
            : PvPMode.instance.getServerProxy ().getConfiguration ().getDefaultPvPMode ().toBoolean ();
    }

    @Override
    public void setPvPEnabled (boolean pvpEnabled)
    {
        pvpDataTag.setBoolean (PVP_ENABLED_NBT_KEY, pvpEnabled);
    }

    @Override
    public long getPvPWarmup ()
    {
        return pvpDataTag.getLong (PVP_WARMUP_NBT_KEY);
    }

    @Override
    public void setPvPWarmup (long pvpWarmup)
    {
        pvpDataTag.setLong (PVP_WARMUP_NBT_KEY, pvpWarmup);
    }

    @Override
    public long getPvPCooldown ()
    {
        return pvpDataTag.getLong (PVP_COOLDOWN_NBT_KEY);
    }

    @Override
    public void setPvPCooldown (long pvpCooldown)
    {
        pvpDataTag.setLong (PVP_COOLDOWN_NBT_KEY, pvpCooldown);
    }

    @Override
    public EnumForcedPvPMode getForcedPvPMode ()
    {
        return EnumForcedPvPMode.values ()[pvpDataTag.getInteger (FORCED_PVP_MODE_NBT_KEY)];
    }

    @Override
    public void setForcedPvPMode (EnumForcedPvPMode forcedPvPMode)
    {
        pvpDataTag.setInteger (FORCED_PVP_MODE_NBT_KEY, forcedPvPMode.ordinal ());
    }

    @Override
    public long getPvPTimer ()
    {
        return pvpDataTag.getLong (PVP_TIMER_NBT_KEY);
    }

    @Override
    public void setPvPTimer (long pvpTimer)
    {
        pvpDataTag.setLong (PVP_TIMER_NBT_KEY, pvpTimer);
    }

    @Override
    public boolean isSpyingEnabled ()
    {
        return pvpDataTag.getBoolean (SPYING_ENABLED_NBT_KEY);
    }

    @Override
    public void setSpyingEnabled (boolean spyingEnabled)
    {
        pvpDataTag.setBoolean (SPYING_ENABLED_NBT_KEY, spyingEnabled);
    }

    @Override
    public boolean isDefaultModeForced ()
    {
        return pvpDataTag.hasKey (DEFAULT_MODE_FORCED_NBT_KEY) ? pvpDataTag.getBoolean (DEFAULT_MODE_FORCED_NBT_KEY)
            : true;
    }

    @Override
    public void setDefaultModeForced (boolean defaultModeForced)
    {
        pvpDataTag.setBoolean (DEFAULT_MODE_FORCED_NBT_KEY, defaultModeForced);
    }

}
