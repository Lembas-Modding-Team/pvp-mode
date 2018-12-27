package pvpmode.api.common.utils;

import java.lang.annotation.*;

/**
 * Used by the dependency injection system to process certain components. How
 * they are processed is up to the providers.
 * 
 * @author CraftedMods
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Process
{

    /**
     * Returns whether the component should be processed or not. If not, the
     * providers mustn't process the component.
     * 
     * @return Whether the component should be processed
     */
    boolean enabled() default true;

    /**
     * Returns an array containing string properties of the component. The providers
     * can process them.
     * 
     * @return The properties of the component
     */
    String[] properties() default {};

}
