package pvpmode.compatibility.events;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

/**
 * An event fired every time before a combat event is logged. If canceled, the
 * event won't be logged.
 * 
 * @author CraftedMods
 *
 */
@Cancelable
public class OnPvPLogEvent extends Event
{

    private final EntityPlayer attacker;
    private final EntityPlayer victim;
    private final float damageAmount;
    private final DamageSource source;

    public OnPvPLogEvent (EntityPlayer attacker, EntityPlayer victim, float damageAmount, DamageSource source)
    {
        this.attacker = attacker;
        this.victim = victim;
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
     * Returns the victim
     */
    public EntityPlayer getVictim ()
    {
        return victim;
    }

    /**
     * Returns the dealt damage amount
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
