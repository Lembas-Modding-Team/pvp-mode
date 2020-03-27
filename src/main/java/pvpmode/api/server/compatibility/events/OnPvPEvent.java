package pvpmode.api.server.compatibility.events;

import java.util.UUID;

import cpw.mods.fml.common.eventhandler.*;
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

    private final UUID attackerUUID;
    private final EnumPvPMode attackerMode;
    private final UUID victimUUID;
    private final EnumPvPMode victimMode;
    private final float damageAmount;
    private final DamageSource source;

    public OnPvPEvent (UUID attackerUUID, EnumPvPMode attackerMode, UUID victimUUID, EnumPvPMode victimMode,
        float damageAmount, DamageSource source)
    {
        this.attackerUUID = attackerUUID;
        this.attackerMode = attackerMode;
        this.victimUUID = victimUUID;
        this.victimMode = victimMode;
        this.damageAmount = damageAmount;
        this.source = source;
    }

    /**
     * Returns the attacker player UUID. The UUID is used because the attacker player could be
     * offline - for example hired units of offline players can still attack online players and their units.
     */
    public UUID getAttackerUUID ()
    {
        return attackerUUID;
    }

    /**
     * Returns the attacker's mode
     */
    public EnumPvPMode getAttackerMode ()
    {
        return attackerMode;
    }

    /**
     * Returns the victim player UUID. The UUID is used because the victim could be
     * offline - for example hired units of offline players could be attacked.
     */
    public UUID getVictimUUID ()
    {
        return victimUUID;
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
