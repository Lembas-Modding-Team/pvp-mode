package pvpmode.internal.server;

import java.util.*;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import pvpmode.PvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;
import pvpmode.api.server.PvPData;
import pvpmode.internal.common.network.PvPStateChangedMessage;

public class PvPDataImpl implements PvPData
{

    private UUID playerUUID;
    private NBTTagCompound pvpDataTag;

    private static final String PVP_ENABLED_NBT_KEY = "PvPEnabled";
    private static final String PVP_WARMUP_NBT_KEY = "PvPWarmup";
    private static final String PVP_COOLDOWN_NBT_KEY = "PvPCooldown";
    private static final String FORCED_PVP_MODE_NBT_KEY = "ForcedPvPMode";
    private static final String PVP_TIMER_NBT_KEY = "PvPTimer";
    private static final String SPYING_ENABLED_NBT_KEY = "Spying";
    private static final String DEFAULT_MODE_FORCED_NBT_KEY = "DefaultModeForced";
    private static final String VAULT_NBT_KEY = "Vault";
    private static final String IS_CREATIVE_CACHE_NBT_KEY = "IsCreativeCache";
    private static final String CAN_FLY_CACHE_NBT_KEY = "CanFlyCache";

    private boolean hasChanged = false;

    public PvPDataImpl (NBTTagCompound pvpDataTag, UUID playerUUID)
    {
        this.pvpDataTag = pvpDataTag;
        this.playerUUID = playerUUID;
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
        if (pvpEnabled != isPvPEnabled ())
        {
            pvpDataTag.setBoolean (PVP_ENABLED_NBT_KEY, pvpEnabled);
            hasChanged = true;
        }
    }

    @Override
    public long getPvPWarmup ()
    {
        return pvpDataTag.getLong (PVP_WARMUP_NBT_KEY);
    }

    @Override
    public void setPvPWarmup (long pvpWarmup)
    {
        if (getPvPWarmup () != pvpWarmup)
        {
            pvpDataTag.setLong (PVP_WARMUP_NBT_KEY, pvpWarmup);
            hasChanged = true;
        }
    }

    @Override
    public long getPvPCooldown ()
    {
        return pvpDataTag.getLong (PVP_COOLDOWN_NBT_KEY);
    }

    @Override
    public void setPvPCooldown (long pvpCooldown)
    {
        if (getPvPCooldown () != pvpCooldown)
        {
            pvpDataTag.setLong (PVP_COOLDOWN_NBT_KEY, pvpCooldown);
            hasChanged = true;
        }
    }

    @Override
    public EnumForcedPvPMode getForcedPvPMode ()
    {
        return EnumForcedPvPMode.values ()[pvpDataTag.getInteger (FORCED_PVP_MODE_NBT_KEY)];
    }

    @Override
    public void setForcedPvPMode (EnumForcedPvPMode forcedPvPMode)
    {
        if (getForcedPvPMode () != forcedPvPMode)
        {
            pvpDataTag.setInteger (FORCED_PVP_MODE_NBT_KEY, forcedPvPMode.ordinal ());
            hasChanged = true;
        }
    }

    @Override
    public long getPvPTimer ()
    {
        return pvpDataTag.getLong (PVP_TIMER_NBT_KEY);
    }

    @Override
    public void setPvPTimer (long pvpTimer)
    {
        if (getPvPTimer () != pvpTimer)
        {
            pvpDataTag.setLong (PVP_TIMER_NBT_KEY, pvpTimer);
            hasChanged = true;

            PvPMode.instance.getServerProxy ().getPacketDispatcher ()
                .sendToAll (new PvPStateChangedMessage (playerUUID, this.getPvPTimer () != 0));
        }
    }

    @Override
    public boolean isSpyingEnabled ()
    {
        return pvpDataTag.getBoolean (SPYING_ENABLED_NBT_KEY);
    }

    @Override
    public void setSpyingEnabled (boolean spyingEnabled)
    {
        if (isSpyingEnabled () != spyingEnabled)
        {
            pvpDataTag.setBoolean (SPYING_ENABLED_NBT_KEY, spyingEnabled);
            hasChanged = true;
        }
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
        if (isDefaultModeForced () != defaultModeForced)
        {
            pvpDataTag.setBoolean (DEFAULT_MODE_FORCED_NBT_KEY, defaultModeForced);
            hasChanged = true;
        }
    }

    @Override
    public List<ItemStack> getVault ()
    {
        List<ItemStack> ret = new ArrayList<> ();
        NBTTagList vaultList = pvpDataTag.getTagList (VAULT_NBT_KEY, 10);
        for (int i = 0; i < vaultList.tagCount (); i++)
        {
            ret.add (ItemStack.loadItemStackFromNBT (vaultList.getCompoundTagAt (i)));
        }
        return ret;
    }

    @Override
    public void setVault (List<ItemStack> vault)
    {
        if (!getVault ().equals (vault))
        {
            NBTTagList vaultList = new NBTTagList ();
            for (ItemStack stack : vault)
            {
                vaultList.appendTag (stack.writeToNBT (new NBTTagCompound ()));
            }

            pvpDataTag.setTag (VAULT_NBT_KEY, vaultList);
            hasChanged = true;
        }
    }

    /*
     * Internal only - saves whether the player is in creative mode. This is
     * necessary to get that information when the player is offline without loading
     * the MC playerdata. The data will be updated when the player leaves/joins the
     * server.
     */
    public boolean isCreativeCache ()
    {
        return pvpDataTag.getBoolean (IS_CREATIVE_CACHE_NBT_KEY);
    }

    public void setCreativeCache (boolean creativeCache)
    {
        if (isCreativeCache () != creativeCache)
        {
            pvpDataTag.setBoolean (IS_CREATIVE_CACHE_NBT_KEY, creativeCache);
            hasChanged = true;
        }
    }

    /*
     * See isCreativeCache
     */
    public boolean canFlyCache ()
    {
        return pvpDataTag.getBoolean (CAN_FLY_CACHE_NBT_KEY);
    }

    public void setCanFlyCache (boolean canFlyCache)
    {
        if (canFlyCache () != canFlyCache)
        {
            pvpDataTag.setBoolean (CAN_FLY_CACHE_NBT_KEY, canFlyCache);
            hasChanged = true;
        }
    }

    public NBTTagCompound getStorageTag ()
    {
        return pvpDataTag;
    }

    public boolean hasChanged ()
    {
        return hasChanged;
    }

    public void setHasChanged (boolean hasChanged)
    {
        this.hasChanged = hasChanged;
    }

}
