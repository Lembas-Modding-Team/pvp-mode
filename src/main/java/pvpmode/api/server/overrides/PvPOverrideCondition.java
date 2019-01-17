package pvpmode.api.server.overrides;

import net.minecraft.entity.player.EntityPlayer;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.common.overrides.EnumForcedPvPMode;

/**
 * The interface for an override condition. Implement it to create custom
 * override conditions.
 *
 * @author CraftedMods
 *
 */
public interface PvPOverrideCondition
{

    /**
     * Returns the priority of this condition.<br>
     * The higher the returned value, the higher the priority. A high priority means
     * that this condition will be preferred over conditions with conflicting
     * results that have a lower priority.
     */
    public int getPriority ();

    /**
     * Returns the overridden PvPMode (only OFF or ON) for the supplied player.
     * <br>
     * It also can return null, which means, that this condition doesn't apply.
     */
    public EnumForcedPvPMode getForcedPvPMode (EntityPlayer player);

    /**
     * Returns a message displayed when the supplied player is forced to the
     * specified PvP Mode. Return null to display no message. If global is true, the
     * message is displayed to all players, if false, it only will be displayed to
     * the supplied player.
     * 
     * @param player
     *            The player which PvP Mode will be forced
     * @param forcedMode
     *            The forced PvP Mode
     * @param global
     *            Whether the message will be sent to all players
     * @return The message
     */
    public String getForcedOverrideMessage (EntityPlayer player, EnumPvPMode forcedMode, boolean global);

}