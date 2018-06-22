package pvpmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import pvpmode.overrides.EnumForcedPvPMode;

/**
 * A wrapper for accessing the data PvPMode stores about each player.
 * 
 * @author CraftedMods
 *
 */
public class PvPData
{
    private NBTTagCompound pvpDataTag;

    private static final String PVP_DATA_NBT_KEY = "PvPData";

    private static final String PVP_ENABLED_NBT_KEY = "PvPEnabled";
    private static final String PVP_WARMUP_NBT_KEY = "PvPWarmup";
    private static final String PVP_COOLDOWN_NBT_KEY = "PvPCooldown";
    private static final String FORCED_PVP_MODE_NBT_KEY = "ForcedPvPMode";
    private static final String PVP_TIMER_NBT_KEY = "PvPTimer";

    public PvPData (EntityPlayer player)
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

    /**
     * Returns whether PvP is enabled for the player. Note that this doesn't
     * mean that the player can actually do PvP - this depends on other
     * parameters like the gamemode. Use {@link PvPUtils#getPvPMode} for this.
     */
    public boolean isPvPEnabled ()
    {
        return pvpDataTag.getBoolean (PVP_ENABLED_NBT_KEY);
    }

    public void setPvPEnabled (boolean pvpEnabled)
    {
        pvpDataTag.setBoolean (PVP_ENABLED_NBT_KEY, pvpEnabled);
    }

    public long getPvPWarmup ()
    {
        return pvpDataTag.getLong (PVP_WARMUP_NBT_KEY);
    }

    public void setPvPWarmup (long pvpWarmup)
    {
        pvpDataTag.setLong (PVP_WARMUP_NBT_KEY, pvpWarmup);
    }

    public long getPvPCooldown ()
    {
        return pvpDataTag.getLong (PVP_COOLDOWN_NBT_KEY);
    }

    public void setPvPCooldown (long pvpCooldown)
    {
        pvpDataTag.setLong (PVP_COOLDOWN_NBT_KEY, pvpCooldown);
    }

    public EnumForcedPvPMode getForcedPvPMode ()
    {
        return EnumForcedPvPMode.values ()[pvpDataTag.getInteger (FORCED_PVP_MODE_NBT_KEY)];
    }

    public void setForcedPvPMode (EnumForcedPvPMode forcedPvPMode)
    {
        pvpDataTag.setInteger (FORCED_PVP_MODE_NBT_KEY, forcedPvPMode.ordinal ());
    }

    public long getPvPTimer ()
    {
        return pvpDataTag.getLong (PVP_TIMER_NBT_KEY);
    }

    public void setPvPTimer (long pvpTimer)
    {
        pvpDataTag.setLong (PVP_TIMER_NBT_KEY, pvpTimer);
    }

}
