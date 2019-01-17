package pvpmode.api.common.configuration.auto;

import java.lang.annotation.*;

/**
 * This annotation allows the specification of bounds for certain configuration
 * properties. The exact interpretation is up to the configuration property key
 * creator. It will only be processed if the relevant method is also annotated
 * with {@link ConfigurationPropertyGetter}
 * 
 * @author CraftedMods
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Bounded
{

    /**
     * The minimum possible value of the configuration property. Use "" to specify
     * the default one (what's default is up to the configuration property key
     * creator). The value is encoded as a string.
     * 
     * @return The minimum value
     */
    public String min() default "";

    /**
     * The maximum possible value of the configuration property. Use "" to specify
     * the default one (what's default is up to the configuration property key
     * creator). The value is encoded as a string.
     * 
     * @return The maximum value
     */
    public String max() default "";

}
