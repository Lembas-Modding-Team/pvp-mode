package pvpmode.api.server;

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

}