package pvpmode.api.common.configuration.auto;

import java.lang.annotation.*;

/**
 * This annotation can be used on methods in a configuration interface annotated
 * with {@link ConfigurationPropertyGetter}. This annotation declares a list of
 * valid values that can be assigned to the property. The exact usage depends on
 * the configuration property key creator.
 * 
 * @author CraftedMods
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Matches
{

    /**
     * Returns a list of valid property values - encoded as a string.
     * 
     * @return A list of valid property values
     */
    public String[] matches();

}
