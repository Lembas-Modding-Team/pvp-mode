package pvpmode.api.common.utils;

import java.lang.annotation.*;

/**
 * An annotation for the dependency injection system used to register certain
 * components - that means, that instances of classes annotated with that
 * property will be created. For that the class needs a zero-argument
 * constructor. How exactly this annotation is processed, is up to the
 * providers.
 * 
 * @author CraftedMods
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Register
{
    /**
     * The name of the component to register. If not specified, an empty string will
     * be used.
     * 
     * @return The name
     */
    String name() default "";

    /**
     * Returns whether the component is enabled or not. If not, no provider is
     * allowed to process the component further.
     * 
     * @return Whether the component is enabled
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
