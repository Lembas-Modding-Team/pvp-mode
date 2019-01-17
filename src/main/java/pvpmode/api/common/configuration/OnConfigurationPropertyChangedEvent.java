package pvpmode.api.common.configuration;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * This event has to be fired every time the configuration property values of a
 * registered configuration property will be changed, also, when the properties
 * were reloaded and the new values aren't equal to the old ones. It doesn’t have
 * to be fired if the property value was changed before it can be accessed by
 * components outside of the configuration environment, for example directly
 * after the properties have been reloaded in the
 * {@link ConfigurationManager#load()} function. It will be fired too when the
 * configuration is loaded for the first time. In that case the old value is
 * null.
 * 
 * @author CraftedMods
 *
 */
public class OnConfigurationPropertyChangedEvent extends Event
{

    private final ConfigurationPropertyKey<?> propertyKey;
    private final Object newValue;
    private final Object oldValue;

    public OnConfigurationPropertyChangedEvent (ConfigurationPropertyKey<?> propertyKey, Object newValue,
        Object oldValue)
    {
        this.propertyKey = propertyKey;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    public ConfigurationPropertyKey<?> getPropertyKey ()
    {
        return propertyKey;
    }

    public Object getNewValue ()
    {
        return newValue;
    }

    public Object getOldValue ()
    {
        return oldValue;
    }

}
