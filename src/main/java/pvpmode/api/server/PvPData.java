package pvpmode.api.server;

import java.util.List;

import net.minecraft.item.ItemStack;
import pvpmode.api.common.overrides.EnumForcedPvPMode;

/**
 * A wrapper for accessing the data PvPMode stores about each player.
 *
 * @author CraftedMods
 *
 */
public interface PvPData
{

    /**
     * Returns whether PvP is enabled for the player. Note that this doesn't mean
     * that the player can actually do PvP - this depends on other parameters like
     * the gamemode.
     */
    public boolean isPvPEnabled ();

    public void setPvPEnabled (boolean pvpEnabled);

    public long getPvPWarmup ();

    public void setPvPWarmup (long pvpWarmup);

    public long getPvPCooldown ();

    public void setPvPCooldown (long pvpCooldown);

    public EnumForcedPvPMode getForcedPvPMode ();

    public void setForcedPvPMode (EnumForcedPvPMode forcedPvPMode);

    public long getPvPTimer ();

    public void setPvPTimer (long pvpTimer);

    public boolean isSpyingEnabled ();

    public void setSpyingEnabled (boolean spyingEnabled);

    public boolean isDefaultModeForced ();

    public void setDefaultModeForced (boolean defaultModeForced);

    /**
     * Returns the vault of the player. It's a list of items, which is saved within
     * the playerdata. One can see it like an additional inventory.
     * 
     * @return The vault
     */
    public List<ItemStack> getVault ();

    public void setVault (List<ItemStack> vault);

}
