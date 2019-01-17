package pvpmode.api.common.configuration.auto;

import pvpmode.api.common.utils.Register;

/**
 * A interface which maps certain property key identifiers to other
 * representations. The mappers can be registered via the {@link Register}
 * annotation. All discovered mappers will be executed in a certain order by the
 * environment. This order can be influenced via the
 * {@link Register#properties()} array, to be specific, with the property
 * {@link AutoConfigurationConstants#PRIORITY_PROPERTY_KEY}. A higher priority
 * means that the mapper will be executed earlier. The mappers will be created
 * before the actual modding environment is loaded (in the core modding stage).
 * 
 * @author CraftedMods
 *
 */
public interface ConfigurationPropertyNameMapper
{

    /**
     * Maps the defined name (the name of the method) to the internal name of the
     * configuration property
     * 
     * @param definedName
     *            The method name
     * @return The internal name
     */
    public String toInternalName (String definedName);

    /**
     * Maps the internal name to a display name. Used to handle the many standard
     * cases where the display name can be derived directly from the internal name.
     * 
     * @param internalName
     *            The internal name
     * @return The display name
     */
    public String toDisplayName (String internalName);

}
