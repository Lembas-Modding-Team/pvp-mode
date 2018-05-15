package pvpmode.log;

import java.nio.file.Path;
import java.util.Date;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

/**
 * A handler for combat logging. If a player or an unit of this player attacks
 * another player, the mod can log this event. To support multiple logging
 * targets (files, different file formats, SQL, E-Mail, ...) the combat logging
 * system uses modules which handle this logging - combat log handlers. Users
 * can activate or deactivate all registered modules via the configuration file.
 * 
 * @author CraftedMods
 *
 */
public interface CombatLogHandler
{
    /**
     * Called on startup when the module is active and should be initialized.
     * 
     * @param pvpLoggingDir
     *            The directory where the handler can store logging data
     */
    public void init(Path pvpLoggingDir);

    /**
     * Invoked when a pvp event should be logged.
     * 
     * @param date
     *            The current date and time
     * @param attacker
     *            The player which initiated the attack
     * @param victim
     *            The player who was attacked
     * @param damageAmount
     *            The amount of damage which was dealt
     * @param damageSource
     *            The damage source
     */
    public void log(Date date, EntityPlayer attacker, EntityPlayer victim, float damageAmount,
                    DamageSource damageSource);

    /**
     * Called on server shutdown when the module is active for cleanup purposes.
     */
    public void cleanup();

}
