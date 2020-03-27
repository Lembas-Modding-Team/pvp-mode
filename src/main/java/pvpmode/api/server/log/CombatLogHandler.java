package pvpmode.api.server.log;

import java.nio.file.Path;
import java.util.*;

import net.minecraft.util.DamageSource;

/**
 * A handler for combat logging. If a player or an unit of this player attacks
 * another player (or unit of that player), the mod can log this event. To support multiple logging
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
     * Called when the module is activated for the first time.
     *
     * @param pvpLoggingDir
     *            The directory where the handler can store logging data
     */
    public void init (Path pvpLoggingDir);

    /**
     * Invoked when a PvP event should be logged.
     *
     * @param date
     *            The current date and time
     * @param attackerUUID
     *            The UUID of the player which initiated the attack (or owned the
     *            entity that initiated it)
     * @param victimUUID
     *            The UUID of the player who was attacked (or owned the entity that
     *            was attacked)
     * @param damageAmount
     *            The amount of damage which was dealt
     * @param damageSource
     *            The damage source
     */
    public void log (Date date, UUID attackerUUID, UUID victimUUID, float damageAmount,
        DamageSource damageSource);

    /**
     * Called on server shutdown when the module was activated.
     */
    public void cleanup ();

}
