package pvpmode.api.common.utils;

import java.lang.annotation.*;

/**
 * This annotation is used by the dependency injection system to inject certain
 * components into a specific field. It can be used on static fields in
 * conjunction with the {@link Process} annotation or in conjunction with
 * registered components with the {@link Register} annotation for simple fields.
 * 
 * @author CraftedMods
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value =
{ElementType.FIELD})
public @interface Inject
{
    /**
     * Returns the name of the component to be injected. If an empty string is
     * specified, the providers can determine the component name by the environment,
     * for example the field name.
     * 
     * @return The name of the component to be injected
     */
    String name() default "";
}
