package pvpmode.api.common.configuration.auto;

import cpw.mods.fml.common.eventhandler.Event;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;

/**
 * An event that will be fired upon the creation of a property key. It can be
 * used to modify the automatically-generated keys dynamically, for example to
 * take other data into account.
 * 
 * @author CraftedMods
 *
 */
public class OnPropertyKeyCreatedEvent extends Event
{

    private final ConfigurationPropertyKey<?> key;
    private ConfigurationPropertyKey<?> resultKey;

    public OnPropertyKeyCreatedEvent (ConfigurationPropertyKey<?> key)
    {
        this.key = key;
        this.resultKey = key;
    }

    /**
     * Returns the key instance created by the auto configuration environment.
     * 
     * @return The generated key instance
     */
    public ConfigurationPropertyKey<?> getKey ()
    {
        return key;
    }

    /**
     * Returns the key instance that will actually be used. The default one is the
     * generated one.
     * 
     * @return The key instance to use
     */
    public ConfigurationPropertyKey<?> getResultKey ()
    {
        return resultKey;
    }

    /**
     * Sets the key instance the will be used
     * 
     * @param resultKey
     *            The new key instance
     */
    public void setResultKey (ConfigurationPropertyKey<?> resultKey)
    {
        this.resultKey = resultKey;
    }

}
