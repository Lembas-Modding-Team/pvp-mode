package pvpmode.api.common.configuration.auto;

import java.lang.annotation.*;

import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;

/**
 * Marks a function in an interface as a configuration property, which will be
 * processed by the Auto Configuration Environment. These methods can return a
 * default value which then is the default value of that property. This
 * annotation allows the specification of certain metadata for the relevant
 * configuration property.
 * 
 * @author CraftedMods
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface ConfigurationPropertyGetter
{

    /**
     * The lowercase, internal name of the configuration property. Use "_" instead
     * of spaces. If not specified, the internal name will be derived from the
     * method name.
     * 
     * @return The internal name
     */
    String internalName() default "";

    /**
     * The category the relevant configuration property is assigned to. How the
     * category name will be processed is up to the provider.
     * 
     * @return The category
     */
    String category();

    /**
     * The unit the configuration property is assigned to. Providers can use that
     * for validation and display purposes.
     * 
     * @return The unit of the property
     */
    Unit unit() default Unit.NONE;

}
