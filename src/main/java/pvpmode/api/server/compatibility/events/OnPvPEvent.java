package pvpmode.api.server.compatibility.events;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import pvpmode.api.common.EnumPvPMode;

/**
 * Fired when a player attacks another player.
 *
 * @author Vinyarion
 *
 */
@Cancelable
public class OnPvPEvent extends Event
{

    private final EntityPlayer attacker;
    private final EnumPvPMode attackerMode;
    private final EntityPlayer victim;
    private final EnumPvPMode victimMode;
    private final float damageAmount;
    private final DamageSource source;

    public OnPvPEvent (EntityPlayer attacker, EnumPvPMode attackerMode, EntityPlayer victim, EnumPvPMode victimMode, float damageAmount, DamageSource source)
    {
        this.attacker = attacker;
        this.attackerMode = attackerMode;
        this.victim = victim;
        this.victimMode = victimMode;
        this.damageAmount = damageAmount;
        this.source = source;
    }

    /**
     * Returns the attacker
     */
    public EntityPlayer getAttacker ()
    {
        return attacker;
    }

    /**
     * Returns the attacker's mode
     */
    public EnumPvPMode getAttackerMode ()
    {
        return attackerMode;
    }

    /**
     * Returns the victim
     */
    public EntityPlayer getVictim ()
    {
        return victim;
    }

    /**
     * Returns the victim's mode
     */
    public EnumPvPMode getVictimMode ()
    {
        return victimMode;
    }

    /**
     * Returns the amount of damage to be dealt
     */
    public float getDamageAmount ()
    {
        return damageAmount;
    }

    /**
     * Returns the damage source
     */
    public DamageSource getSource ()
    {
        return source;
    }

}
