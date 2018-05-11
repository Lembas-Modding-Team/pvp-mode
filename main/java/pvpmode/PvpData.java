package pvpmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A wrapper for accessing the data PvpMode stores about each player.
 * 
 * @author CraftedMods
 *
 */
public class PvpData
{
    private NBTTagCompound pvpDataTag;

    private static final String PVP_DATA_NBT_KEY = "PvPData";

    private static final String PVP_ENABLED_NBT_KEY = "PvPEnabled";
    private static final String PVP_WARMUP_NBT_KEY = "PvPWarmup";
    private static final String PVP_COOLDOWN_NBT_KEY = "PvPCooldown";
    private static final String PVP_TAG_NBT_KEY = "PvPTag";

    public PvpData (EntityPlayer player)
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

    public boolean isPvpEnabled ()
    {
        return pvpDataTag.getBoolean (PVP_ENABLED_NBT_KEY);
    }

    public void setPvpEnabled (boolean pvpEnabled)
    {
        pvpDataTag.setBoolean (PVP_ENABLED_NBT_KEY, pvpEnabled);
    }

    public long getPvpWarmup ()
    {
        return pvpDataTag.getLong (PVP_WARMUP_NBT_KEY);
    }

    public void setPvpWarmup (long pvpWarmup)
    {
        pvpDataTag.setLong (PVP_WARMUP_NBT_KEY, pvpWarmup);
    }

    public long getPvpCooldown ()
    {
        return pvpDataTag.getLong (PVP_COOLDOWN_NBT_KEY);
    }

    public void setPvpCooldown (long pvpCooldown)
    {
        pvpDataTag.setLong (PVP_COOLDOWN_NBT_KEY, pvpCooldown);
    }

    public long getPvpTag ()
    {
        return pvpDataTag.getLong (PVP_TAG_NBT_KEY);
    }

    public void setPvpTag (long pvpTag)
    {
        pvpDataTag.setLong (PVP_TAG_NBT_KEY, pvpTag);
    }

}
