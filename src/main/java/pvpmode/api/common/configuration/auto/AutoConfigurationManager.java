package pvpmode.api.common.configuration.auto;

import pvpmode.api.common.configuration.ConfigurationManager;

/**
 * The interface for automatically managed configuration managers. That means,
 * that the configuration properties can be specified via annotated methods of
 * the interface, and the auto configuration environment will take care of the
 * correct handling of these properties. The manager can be annotated with the
 * {@link pvpmode.api.common.utils.Process} annotation, so it'll be processed
 * automatically via the auto configuration system. If the property
 * {@link AutoConfigurationConstants#MANUAL_PROCESSING_PROPERTY_KEY} is used
 * with the value \"true\", the manager has to be submitted manually to the
 * processing instance.
 * 
 * @author CraftedMods
 *
 */
public interface AutoConfigurationManager extends ConfigurationManager
{

    /**
     * Returns the PID (Persistence ID) of the configuration entries. The PID will
     * allow the environment the assignment of the specified configuration
     * properties to providers. If no PID is specified,
     * {@link AutoConfigurationConstants#DEFAULT_PID} will be returned. This is
     * usually a sign that providers should try to return the PID that was specified
     * via the {@link pvpmode.api.common.utils.Process} annotation.
     * 
     * @return The PID of this configuration manager
     */
    public default String getPID ()
    {
        return AutoConfigurationConstants.DEFAULT_PID;
    }
}
