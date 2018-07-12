package pvpmode.overrides;

import net.minecraft.entity.player.EntityPlayer;

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
     * Returns the priority of this condition.<br/>
     * The higher the returned value, the higher the priority. A high priority means
     * that this condition will be preferred over conditions with conflicting
     * results that have a lower priority.
     */
    public int getPriority ();

    /**
     * Returns the overridden PvPMode (only OFF or ON) for the supplied player.
     * <br/>
     * It also can return null, which means, that this condition doesn't apply.
     */
    public Boolean isPvPEnabled (EntityPlayer player);

    /**
     * Returns a message displayed to all users for the supplied player with the
     * supplied forced mode when this player is forced into the supplied mode.
     * Return null to display no message.
     */
    public String getForcedOverrideMessage (EntityPlayer player, Boolean mode);

}