package pvpmode.api.server.compatibility.events;

import java.util.UUID;

import cpw.mods.fml.common.eventhandler.*;
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

    private final UUID attackerUUID;
    private final UUID victimUUID;
    private final float damageAmount;
    private final DamageSource source;

    public OnPvPLogEvent (UUID attackerUUID, UUID victimUUID, float damageAmount, DamageSource source)
    {
        this.attackerUUID = attackerUUID;
        this.victimUUID = victimUUID;
        this.damageAmount = damageAmount;
        this.source = source;
    }

    /**
     * Returns the UUID of the attacker
     */
    public UUID getAttackerUUID ()
    {
        return attackerUUID;
    }

    /**
     * Returns the victim player UUID
     */
    public UUID getVictimUUID ()
    {
        return victimUUID;
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
